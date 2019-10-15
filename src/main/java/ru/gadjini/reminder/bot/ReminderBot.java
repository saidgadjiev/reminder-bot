package ru.gadjini.reminder.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.properties.BotProperties;
import ru.gadjini.reminder.service.CommandNavigator;
import ru.gadjini.reminder.service.CommandRegistry;
import ru.gadjini.reminder.service.MessageService;

@Component
public class ReminderBot extends TelegramLongPollingBot {

    private BotProperties botProperties;

    private CommandRegistry commandRegistry;

    private CommandNavigator commandNavigator;

    private MessageService messageService;

    @Autowired
    public ReminderBot(BotProperties botProperties,
                       CommandRegistry commandRegistry,
                       CommandNavigator commandNavigator,
                       MessageService messageService) {
        this.botProperties = botProperties;
        this.commandRegistry = commandRegistry;
        this.commandNavigator = commandNavigator;
        this.messageService = messageService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            restoreIfNeed(update.getMessage().getChatId(), update.getMessage().getText().trim());

            if (commandRegistry.isCommand(update.getMessage())) {
                if (!commandRegistry.executeCommand(this, update.getMessage())) {
                    messageService.sendMessageByCode(update.getMessage().getChatId(), MessagesProperties.MESSAGE_UNKNOWN_COMMAND);
                }
            } else {
                commandRegistry.processNonCommandUpdate(this, update.getMessage());
            }
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

    private void restoreIfNeed(long chatId, String command) {
        if (command.startsWith(BotCommand.COMMAND_INIT_CHARACTER + MessagesProperties.START_COMMAND_NAME)) {
            return;
        }
        if (commandNavigator.isEmpty(chatId)) {
            commandNavigator.zeroRestore(chatId, (NavigableBotCommand) commandRegistry.getBotCommand(MessagesProperties.START_COMMAND_NAME));
        }
    }
}
