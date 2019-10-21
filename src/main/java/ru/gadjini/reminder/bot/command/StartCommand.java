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
import ru.gadjini.reminder.util.RequestHelper;
import ru.gadjini.reminder.util.UserUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartCommand extends BotCommand implements NavigableBotCommand {

    private static final List<Function<String, ReminderRequest>> REQUEST_EXTRACTORS = new ArrayList<>() {{
        add(new Function<>() {

            Pattern PATTERN = Pattern.compile("^@([0-9a-zA-Z_]+) (.*) ((2[0-3]|[01]?[0-9]):([0-5]?[0-9]))");

            @Override
            public ReminderRequest apply(String s) {
                Matcher matcher = PATTERN.matcher(s);

                if (matcher.matches()) {
                    ReminderRequest reminderRequest = new ReminderRequest();

                    reminderRequest.setReceiverName(matcher.group(1));
                    reminderRequest.setText(matcher.group(2));

                    LocalTime localTime = LocalTime.parse(matcher.group(3));
                    LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), localTime);
                    reminderRequest.setRemindAt(localDateTime);

                    return reminderRequest;
                }

                return null;
            }
        });
    }};

    private final MessageService messageService;

    private final ReminderService reminderService;

    private TgUserService tgUserService;

    private ReminderTextBuilder reminderTextBuilder;

    private KeyboardService keyboardService;

    public StartCommand(MessageService messageService, ReminderService reminderService, TgUserService tgUserService, ReminderTextBuilder reminderTextBuilder, KeyboardService keyboardService) {
        super(MessagesProperties.START_COMMAND_NAME, "");
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.tgUserService = tgUserService;
        this.reminderTextBuilder = reminderTextBuilder;
        this.keyboardService = keyboardService;
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
        messageService.sendMessageByCode(defaultMemento.getChatId(), MessagesProperties.MESSAGE_MAIN_MENU, keyboardService.getMainMenu());
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        ReminderRequest reminderRequest = RequestHelper.findCandidate(REQUEST_EXTRACTORS, message.getText().trim());

        if (reminderRequest == null) {
            return;
        }
        Reminder reminder = reminderService.createReminder(reminderRequest);
        String reminderText = reminderTextBuilder.create(reminderRequest.getText(), reminderRequest.getRemindAt());

        sendMessages(reminder, reminderText);
    }

    private void sendMessages(Reminder reminder, String reminderText) {
        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_FROM,
                new Object[]{UserUtils.userLink(reminder.getCreator()), reminderText});
        messageService.sendMessageByCode(reminder.getCreator().getChatId(), MessagesProperties.MESSAGE_REMINDER_CREATED,
                new Object[]{reminderText, UserUtils.userLink(reminder.getReceiver())});
    }
}
