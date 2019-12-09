package ru.gadjini.reminder.bot.command;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;

public class StartCommand extends BotCommand implements NavigableBotCommand {

    private final MessageService messageService;

    private final ReminderRequestService reminderService;

    private TgUserService tgUserService;

    private KeyboardService keyboardService;

    private ReminderMessageSender reminderMessageSender;

    public StartCommand(MessageService messageService,
                        ReminderRequestService reminderService,
                        TgUserService tgUserService,
                        KeyboardService keyboardService,
                        ReminderMessageSender reminderMessageSender) {
        super(MessagesProperties.START_COMMAND_NAME, "");
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.tgUserService = tgUserService;
        this.keyboardService = keyboardService;
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
    public void restore(long chatId) {
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_START, keyboardService.getMainMenu());
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        Reminder reminder = reminderService.createReminder(message.getText().trim());
        reminder.getCreator().setChatId(message.getChatId());

        reminderMessageSender.sendReminderCreated(reminder, null);
    }
}
