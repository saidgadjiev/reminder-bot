package ru.gadjini.reminder.bot.command;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class StartCommand extends BotCommand implements NavigableBotCommand {

    private final MessageService messageService;

    private final ReminderRequestService reminderService;

    private TgUserService tgUserService;

    private ReplyKeyboardService replyKeyboardService;

    private ReminderMessageSender reminderMessageSender;

    private UserReminderNotificationService userReminderNotificationService;

    @Autowired
    public StartCommand(MessageService messageService,
                        ReminderRequestService reminderService,
                        TgUserService tgUserService,
                        ReplyKeyboardService replyKeyboardService,
                        ReminderMessageSender reminderMessageSender,
                        UserReminderNotificationService userReminderNotificationService) {
        super(MessagesProperties.START_COMMAND_NAME, "");
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.tgUserService = tgUserService;
        this.replyKeyboardService = replyKeyboardService;
        this.reminderMessageSender = reminderMessageSender;
        this.userReminderNotificationService = userReminderNotificationService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] args) {
        tgUserService.createOrUpdateUser(chat.getId(), user);
        createUserNotifications(user.getId());
        messageService.sendMessageByCode(chat.getId(), MessagesProperties.MESSAGE_START, replyKeyboardService.getMainMenu());
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.START_COMMAND_NAME;
    }

    @Override
    public ReplyKeyboardMarkup silentRestore() {
        return replyKeyboardService.getMainMenu();
    }

    @Override
    public void restore(long chatId) {
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_START, replyKeyboardService.getMainMenu());
    }

    @Override
    public void processNonCommandUpdate(Message message, String reminderText) {
        reminderText = StringUtils.capitalize(reminderText);
        Reminder reminder = reminderService.createReminder(reminderText, null);
        reminder.getCreator().setChatId(message.getChatId());

        reminderMessageSender.sendReminderCreated(reminder, null);
    }

    private void createUserNotifications(int userId) {
        int countWithTime = userReminderNotificationService.count(userId, UserReminderNotification.NotificationType.WITH_TIME);
        if (countWithTime == 0) {
            userReminderNotificationService.createDefaultNotificationsForWithTime(userId);
        }

        int countWithoutTime = userReminderNotificationService.count(userId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        if (countWithoutTime == 0) {
            userReminderNotificationService.createDefaultNotificationsForWithoutTime(userId);
        }
    }
}
