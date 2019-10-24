package ru.gadjini.reminder.bot;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.properties.BotProperties;
import ru.gadjini.reminder.service.CommandNavigator;
import ru.gadjini.reminder.service.CommandRegistry;
import ru.gadjini.reminder.service.KeyboardService;
import ru.gadjini.reminder.service.MessageService;

@Component
public class ReminderBot extends WorkerUpdatesBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderBot.class);

    private BotProperties botProperties;

    private CommandRegistry commandRegistry;

    private CommandNavigator commandNavigator;

    private MessageService messageService;

    private KeyboardService keyboardService;

    @Autowired
    public ReminderBot(BotProperties botProperties,
                       CommandRegistry commandRegistry,
                       CommandNavigator commandNavigator,
                       MessageService messageService,
                       KeyboardService keyboardService) {
        this.botProperties = botProperties;
        this.commandRegistry = commandRegistry;
        this.commandNavigator = commandNavigator;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
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

                if (commandRegistry.isCommand(update.getMessage())) {
                    if (!commandRegistry.executeCommand(this, update.getMessage())) {
                        messageService.sendMessageByCode(update.getMessage().getChatId(), MessagesProperties.MESSAGE_UNKNOWN_COMMAND);
                    }
                } else {
                    commandRegistry.processNonCommandUpdate(this, update.getMessage());
                }
            } else if (update.hasCallbackQuery()) {
                chatId = update.getCallbackQuery().getMessage().getChatId();
                commandRegistry.executeCallbackCommand(update.getCallbackQuery());
            }
        } catch (Exception ex) {
            messageService.sendErrorMessage(chatId, null);
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

    private boolean restoreIfNeed(long chatId, String command) {
        if (StringUtils.isNotBlank(command) && command.startsWith(BotCommand.COMMAND_INIT_CHARACTER + MessagesProperties.START_COMMAND_NAME)) {
            return false;
        }
        if (commandNavigator.isEmpty(chatId)) {
            commandNavigator.zeroRestore(chatId, (NavigableBotCommand) commandRegistry.getBotCommand(MessagesProperties.START_COMMAND_NAME));
            messageService.sendErrorMessage(chatId, keyboardService.getMainMenu());

            return true;
        }

        return false;
    }
}
