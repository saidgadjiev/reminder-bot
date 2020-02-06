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
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class UserReminderNotificationCommand implements KeyboardBotCommand, NavigableBotCommand {

    private Set<String> names = new HashSet<>();

    private MessageService messageService;

    private LocalisationService localisationService;

    private ReplyKeyboardService replyKeyboardService;

    private TgUserService userService;

    @Autowired
    public UserReminderNotificationCommand(MessageService messageService, LocalisationService localisationService, CurrReplyKeyboard replyKeyboardService, TgUserService userService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.replyKeyboardService = replyKeyboardService;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        Locale locale = userService.getLocale(message.getFrom().getId());
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_REMINDER_NOTIFICATION, locale))
                        .replyKeyboard(replyKeyboardService.getUserReminderNotificationSettingsKeyboard(message.getChatId(), locale))
        );
        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.USER_REMINDER_NOTIFICATION_HISTORY_NAME;
    }

    @Override
    public void restore(Message message) {
        Locale locale = userService.getLocale(message.getFrom().getId());
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_REMINDER_NOTIFICATION, locale))
                        .replyKeyboard(replyKeyboardService.getUserReminderNotificationSettingsKeyboard(message.getChatId(), locale))
        );
    }

    @Override
    public ReplyKeyboardMarkup getKeyboard(long chatId) {
        Locale locale = userService.getLocale((int) chatId);
        return replyKeyboardService.getUserReminderNotificationSettingsKeyboard(chatId, locale);
    }
}
