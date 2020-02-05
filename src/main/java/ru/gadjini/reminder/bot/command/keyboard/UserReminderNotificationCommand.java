package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class UserReminderNotificationCommand implements KeyboardBotCommand, NavigableBotCommand {

    private String name;

    private MessageService messageService;

    private LocalisationService localisationService;

    private ReplyKeyboardService replyKeyboardService;

    @Autowired
    public UserReminderNotificationCommand(MessageService messageService, LocalisationService localisationService, CurrReplyKeyboard replyKeyboardService) {
        this.name = localisationService.getCurrentLocaleMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_COMMAND_NAME);
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_REMINDER_NOTIFICATION))
                        .replyKeyboard(replyKeyboardService.getUserReminderNotificationSettingsKeyboard(message.getChatId()))
        );
        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.USER_REMINDER_NOTIFICATION_HISTORY_NAME;
    }

    @Override
    public void restore(long chatId) {
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_REMINDER_NOTIFICATION))
                        .replyKeyboard(replyKeyboardService.getUserReminderNotificationSettingsKeyboard(chatId))
        );
    }

    @Override
    public ReplyKeyboardMarkup getKeyboard(long chatId) {
        return replyKeyboardService.getUserReminderNotificationSettingsKeyboard(chatId);
    }
}
