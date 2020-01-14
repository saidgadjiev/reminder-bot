package ru.gadjini.reminder.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.command.CommandExecutor;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.message.MessageTextExtractor;
import ru.gadjini.reminder.service.metric.LatencyMeter;
import ru.gadjini.reminder.service.metric.LatencyMeterFactory;

@Component
public class ReminderBotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderBotService.class);

    private CommandExecutor commandExecutor;

    private CommandNavigator commandNavigator;

    private MessageService messageService;

    private CurrReplyKeyboard replyKeyboardService;

    private MessageTextExtractor messageTextExtractor;

    private LatencyMeterFactory latencyMeterFactory;

    private LocalisationService localisationService;

    @Autowired
    public ReminderBotService(CommandExecutor commandExecutor,
                              CommandNavigator commandNavigator,
                              MessageService messageService,
                              CurrReplyKeyboard replyKeyboardService,
                              MessageTextExtractor messageTextExtractor,
                              LatencyMeterFactory latencyMeterFactory, LocalisationService localisationService) {
        this.commandExecutor = commandExecutor;
        this.commandNavigator = commandNavigator;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.messageTextExtractor = messageTextExtractor;
        this.latencyMeterFactory = latencyMeterFactory;
        this.localisationService = localisationService;
    }

    public void handleUpdate(Update update) {
        try {
            if (update.hasMessage()) {
                if (restoreCommand(
                        update.getMessage().getChatId(),
                        update.getMessage().hasText() ? update.getMessage().getText().trim() : null
                )) {
                    return;
                }

                LatencyMeter latencyMeter = latencyMeterFactory.getMeter();
                latencyMeter.start();

                String text = messageTextExtractor.extract(update.getMessage());
                handleMessage(update.getMessage(), text);

                if (update.getMessage().hasVoice()) {
                    latencyMeter.stop("Latency on voice request: {}", text);
                } else {
                    latencyMeter.stop("Latency on request: {}", text);
                }
            } else if (update.hasCallbackQuery()) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                commandExecutor.executeCallbackCommand(update.getCallbackQuery());

                stopWatch.stop();
                LOGGER.debug("Latency on callback request: {} = {}", update.getCallbackQuery().getData(), stopWatch.getTime());
            } else if (update.hasEditedMessage()) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                String text = messageTextExtractor.extract(update.getEditedMessage());
                handleEditedMessage(update.getEditedMessage(), text);

                stopWatch.stop();
                LOGGER.debug("Latency on edit message request: {} = {}", text, stopWatch.getTime());
            }
        } catch (UserException ex) {
            LOGGER.error(ex.getMessage());
            messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(TgMessage.getChatId(update)).text(ex.getMessage()));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            messageService.sendErrorMessage(TgMessage.getChatId(update));
        }
    }

    private void handleMessage(Message message, String text) {
        if (commandExecutor.isKeyboardCommand(message.getChatId(), text)) {
            if (isOnCurrentMenu(message.getChatId(), text)) {
                commandExecutor.executeKeyBoardCommand(message, text);

                return;
            }
        } else if (commandExecutor.isBotCommand(message)) {
            if (commandExecutor.executeBotCommand(message)) {
                return;
            } else {
                messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(message.getChatId()).text(localisationService.getMessage(MessagesProperties.MESSAGE_UNKNOWN_COMMAND)));
            }
        } else if (commandExecutor.isTextCommand(message.getChatId(), text)) {
            commandExecutor.executeKeyBoardCommand(message, text);

            return;
        }

        commandExecutor.processNonCommandUpdate(message, text);
    }

    private void handleEditedMessage(Message editedMessage, String text) {
        if (commandExecutor.isKeyboardCommand(editedMessage.getChatId(), text)) {
            commandExecutor.executeKeyBoardCommandEditedMessage(editedMessage, text);
        } else {
            commandExecutor.processNonCommandEditedMessage(editedMessage, text);
        }
    }

    private boolean restoreCommand(long chatId, String command) {
        if (StringUtils.isNotBlank(command) && command.startsWith(BotCommand.COMMAND_INIT_CHARACTER + CommandNames.START_COMMAND_NAME)) {
            return false;
        }
        if (commandNavigator.isEmpty(chatId)) {
            commandNavigator.zeroRestore(chatId, (NavigableBotCommand) commandExecutor.getBotCommand(CommandNames.START_COMMAND_NAME));
            messageService.sendBotRestartedMessage(chatId, replyKeyboardService.getMainMenu(chatId, (int) chatId));

            return true;
        }

        return false;
    }

    private boolean isOnCurrentMenu(long chatId, String commandText) {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardService.getCurrentReplyKeyboard(chatId);

        if (replyKeyboardMarkup == null) {
            return true;
        }

        for (KeyboardRow keyboardRow : replyKeyboardMarkup.getKeyboard()) {
            if (keyboardRow.stream().anyMatch(keyboardButton -> keyboardButton.getText().equals(commandText))) {
                return true;
            }
        }

        return false;
    }
}
