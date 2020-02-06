package ru.gadjini.reminder.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class StartCommand extends BotCommand implements NavigableBotCommand, KeyboardBotCommand {

    private MessageService messageService;

    private ReminderRequestService reminderRequestService;

    private ReplyKeyboardService replyKeyboardService;

    private ReminderMessageSender reminderMessageSender;

    private LocalisationService localisationService;

    private TgUserService userService;

    private Set<String> names = new HashSet<>();

    @Autowired
    public StartCommand(MessageService messageService,
                        ReminderRequestService reminderRequestService,
                        CurrReplyKeyboard replyKeyboardService,
                        ReminderMessageSender reminderMessageSender, LocalisationService localisationService, TgUserService userService) {
        super(CommandNames.START_COMMAND_NAME, "");
        this.messageService = messageService;
        this.reminderRequestService = reminderRequestService;
        this.replyKeyboardService = replyKeyboardService;
        this.reminderMessageSender = reminderMessageSender;
        this.localisationService = localisationService;
        this.userService = userService;
        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.MAIN_MENU_COMMAND_NAME, locale));
        }
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] args) {
        sendMainMenu(user.getId(), localisationService.getCurrentLocale(user.getLanguageCode()));
    }

    @Override
    public String getHistoryName() {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public ReplyKeyboardMarkup getKeyboard(long chatId) {
        Locale locale = userService.getLocale((int) chatId);
        return replyKeyboardService.getMainMenu(chatId, locale);
    }

    @Override
    public boolean accept(Message message) {
        return message.hasText() || message.hasVoice();
    }

    @Override
    public void restore(Message message) {
        Locale locale = userService.getLocale(message.getFrom().getId());
        sendMainMenu(message.getChatId().intValue(), locale);
    }

    @Override
    public void processNonCommandUpdate(Message message, String reminderText) {
        Reminder reminder = reminderRequestService.createReminder(
                new ReminderRequestContext()
                        .setText(reminderText)
                        .setVoice(message.hasVoice())
                        .setUser(message.getFrom())
                        .setMessageId(message.getMessageId()));
        reminderMessageSender.sendReminderCreated(reminder);
    }

    @Override
    public void processNonCommandEditedMessage(Message editedMessage, String text) {
        UpdateReminderResult updateReminderResult = reminderRequestService.updateReminder(editedMessage.getMessageId(), editedMessage.getFrom(), text);
        if (updateReminderResult == null) {
            return;
        }
        reminderMessageSender.sendReminderFullyUpdate(updateReminderResult);
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        Locale locale = userService.getLocale(message.getFrom().getId());
        sendMainMenu(message.getFrom().getId(), locale);

        return true;
    }

    private void sendMainMenu(int userId, Locale locale) {
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(userId)
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_START, locale))
                        .replyKeyboard(replyKeyboardService.getMainMenu(userId, locale)));
    }
}
