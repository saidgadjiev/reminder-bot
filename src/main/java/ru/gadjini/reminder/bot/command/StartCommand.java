package ru.gadjini.reminder.bot.command;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.CommandMemento;
import ru.gadjini.reminder.bot.command.api.DefaultMemento;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.resolver.ReminderRequestResolver;
import ru.gadjini.reminder.service.validation.ErrorBag;
import ru.gadjini.reminder.service.validation.ValidationService;

public class StartCommand extends BotCommand implements NavigableBotCommand {

    private final MessageService messageService;

    private final ReminderService reminderService;

    private TgUserService tgUserService;

    private ReminderRequestResolver reminderRequestResolver;

    private KeyboardService keyboardService;

    private ValidationService validationService;

    private ReminderMessageSender reminderMessageSender;

    public StartCommand(MessageService messageService,
                        ReminderService reminderService,
                        TgUserService tgUserService,
                        ReminderRequestResolver reminderRequestResolver,
                        KeyboardService keyboardService,
                        ValidationService validationService,
                        ReminderMessageSender reminderMessageSender) {
        super(MessagesProperties.START_COMMAND_NAME, "");
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.tgUserService = tgUserService;
        this.reminderRequestResolver = reminderRequestResolver;
        this.keyboardService = keyboardService;
        this.validationService = validationService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] args) {
        tgUserService.createOrUpdateUser(chat.getId(), user);
        messageService.sendMessageByCode(chat.getId(), MessagesProperties.MESSAGE_START, keyboardService.getMainMenu());
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.START_COMMAND_NAME;
    }

    @Override
    public ReplyKeyboardMarkup silentRestore() {
        return keyboardService.getMainMenu();
    }

    @Override
    public void restore(CommandMemento commandMemento) {
        DefaultMemento defaultMemento = (DefaultMemento) commandMemento;
        messageService.sendMessageByCode(defaultMemento.getChatId(), MessagesProperties.MESSAGE_START, keyboardService.getMainMenu());
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        ReminderRequest reminderRequest = reminderRequestResolver.resolve(message.getText().trim());

        if (reminderRequest == null) {
            messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_REMINDER_FORMAT);
            return;
        }
        ErrorBag errorBag = validationService.validate(reminderRequest);

        if (errorBag.hasErrors()) {
            String firstError = errorBag.firstErrorMessage();

            messageService.sendMessage(message.getChatId(), firstError, null);
            return;
        }

        Reminder reminder = reminderService.createReminder(reminderRequest);

        reminderMessageSender.sendReminderCreated(reminder, null);
    }
}
