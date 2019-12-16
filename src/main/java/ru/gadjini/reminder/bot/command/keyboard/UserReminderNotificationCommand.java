package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class UserReminderNotificationCommand implements KeyboardBotCommand, NavigableBotCommand {

    private String name;

    private MessageService messageService;

    private LocalisationService localisationService;

    private KeyboardService keyboardService;

    @Autowired
    public UserReminderNotificationCommand(MessageService messageService, LocalisationService localisationService, KeyboardService keyboardService) {
        this.name = localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_COMMAND_NAME);
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.keyboardService = keyboardService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        messageService.sendMessage(
                message.getChatId(),
                localisationService.getMessage(MessagesProperties.MESSAGE_USER_REMINDER_NOTIFICATION),
                keyboardService.getUserReminderNotificationKeyboard()
        );
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.USER_REMINDER_NOTIFICATION_HISTORY_NAME;
    }

    @Override
    public String getParentHistoryName() {
        return MessagesProperties.START_COMMAND_NAME;
    }

    @Override
    public void restore(long chatId) {
        messageService.sendMessage(
                chatId,
                localisationService.getMessage(MessagesProperties.MESSAGE_USER_REMINDER_NOTIFICATION),
                keyboardService.getUserReminderNotificationKeyboard()
        );
    }

    @Override
    public ReplyKeyboardMarkup silentRestore() {
        return keyboardService.getUserReminderNotificationKeyboard();
    }
}
