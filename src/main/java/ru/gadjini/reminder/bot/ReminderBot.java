package ru.gadjini.reminder.bot;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.properties.BotProperties;
import ru.gadjini.reminder.service.command.CommandExecutor;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.message.MessageTextExtractor;

@Component
public class ReminderBot extends WorkerUpdatesBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderBot.class);

    private BotProperties botProperties;

    private CommandExecutor commandExecutor;

    private CommandNavigator commandNavigator;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private MessageTextExtractor messageTextExtractor;

    @Autowired
    public ReminderBot(BotProperties botProperties,
                       CommandExecutor commandExecutor,
                       CommandNavigator commandNavigator,
                       MessageService messageService,
                       ReplyKeyboardService replyKeyboardService,
                       MessageTextExtractor messageTextExtractor) {
        this.botProperties = botProperties;
        this.commandExecutor = commandExecutor;
        this.commandNavigator = commandNavigator;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.messageTextExtractor = messageTextExtractor;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = -1L;

        try {
            if (update.hasMessage()) {
                chatId = update.getMessage().getChatId();
                if (restoreIfNeed(update.getMessage().getChatId(), update.getMessage().hasText() ? update.getMessage().getText().trim() : null)) {
                    return;
                }

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                String text = messageTextExtractor.extract(update.getMessage());
                handleMessage(update.getMessage(), text);

                stopWatch.stop();
                if (update.getMessage().hasVoice()) {
                    LOGGER.debug("Latency on voice request: {} = {}", text, stopWatch.getTime());
                } else {
                    LOGGER.debug("Latency on request: {} = {}", text, stopWatch.getTime());
                }
            } else if (update.hasCallbackQuery()) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                chatId = update.getCallbackQuery().getMessage().getChatId();
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
            messageService.sendMessage(chatId, ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            messageService.sendErrorMessage(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public String getBotPath() {
        return botProperties.getName();
    }

    private void handleMessage(Message message, String text) {
        if (commandExecutor.isCommand(message, text)) {
            if (!commandExecutor.executeCommand(message, text)) {
                messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_UNKNOWN_COMMAND);
            }
        } else {
            commandExecutor.processNonCommandUpdate(message, text);
        }
    }

    private void handleEditedMessage(Message editedMessage, String text) {
        if (commandExecutor.isKeyboardCommand(text)) {
            commandExecutor.executeKeyBoardCommandEditedMessage(editedMessage, text);
        } else {
            commandExecutor.processNonCommandEditedMessage(editedMessage, text);
        }
    }

    private boolean restoreIfNeed(long chatId, String command) {
        if (StringUtils.isNotBlank(command) && command.startsWith(BotCommand.COMMAND_INIT_CHARACTER + MessagesProperties.START_COMMAND_NAME)) {
            return false;
        }
        if (commandNavigator.isEmpty(chatId)) {
            commandNavigator.zeroRestore(chatId, (NavigableBotCommand) commandExecutor.getBotCommand(MessagesProperties.START_COMMAND_NAME));
            messageService.sendBotRestartedMessage(chatId, replyKeyboardService.getMainMenu());

            return true;
        }

        return false;
    }
}
