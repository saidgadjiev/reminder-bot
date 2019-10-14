package ru.gadjini.reminder.bot.command;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.service.ReminderService;
import ru.gadjini.reminder.service.TgUserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public StartCommand(MessageService messageService, ReminderService reminderService, TgUserService tgUserService) {
        super(MessagesProperties.START_COMMAND_NAME, "");
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.tgUserService = tgUserService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] args) {
        tgUserService.createOrUpdateUser(chat.getId(), user);
        messageService.sendMessageByCode(absSender, chat.getId(), MessagesProperties.MESSAGE_START);
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.START_COMMAND_NAME;
    }

    @Override
    public void processNonCommandUpdate(AbsSender absSender, Message message) {
        String text = message.getText().trim();
        ReminderRequest reminderRequest = null;

        for (Function<String, ReminderRequest> requestFunction : REQUEST_EXTRACTORS) {
            reminderRequest = requestFunction.apply(text);

            if (reminderRequest != null) {
                break;
            }
        }
        reminderRequest.setCreatorName(message.getFrom().getUserName());

        reminderService.createReminder(reminderRequest);
        TgUser tgUser = tgUserService.getUserByUserName(reminderRequest.getReceiverName());

        String reminderText = reminderRequest.getText() + " " + dateTimeFormatter.format(reminderRequest.getRemindAt());
        messageService.sendMessageByCode(absSender, tgUser.getChatId(), MessagesProperties.MESSAGE_REMINDER_FROM,
                new Object[]{ TgUser.USERNAME_START + reminderRequest.getCreatorName(), reminderText});
        messageService.sendMessageByCode(absSender, message.getChatId(), MessagesProperties.MESSAGE_REMINDER_CREATED,
                new Object[]{reminderText, TgUser.USERNAME_START + reminderRequest.getReceiverName()});
    }
}
