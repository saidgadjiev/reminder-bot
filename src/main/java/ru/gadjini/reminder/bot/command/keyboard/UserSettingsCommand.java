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
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class UserSettingsCommand implements KeyboardBotCommand, NavigableBotCommand {

    private Set<String> names = new HashSet<>();

    private final LocalisationService localisationService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private TgUserService userService;

    @Autowired
    public UserSettingsCommand(LocalisationService localisationService, MessageService messageService, CurrReplyKeyboard replyKeyboardService, TgUserService userService) {
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.USER_SETTINGS_COMMAND_NAME, locale));
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
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_USER_SETTINGS, locale))
                        .replyKeyboard(replyKeyboardService.getUserSettingsKeyboard(message.getChatId(), locale))
        );
        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.USER_SETTINGS_COMMAND_HISTORY_NAME;
    }

    @Override
    public void restore(TgMessage message) {
        Locale locale = userService.getLocale(message.getUser().getId());

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_USER_SETTINGS, locale))
                        .replyKeyboard(replyKeyboardService.getUserSettingsKeyboard(message.getChatId(), locale))
        );
    }

    @Override
    public ReplyKeyboardMarkup getKeyboard(long chatId) {
        Locale locale = userService.getLocale((int) chatId);
        return replyKeyboardService.getUserSettingsKeyboard(chatId, locale);
    }
}
