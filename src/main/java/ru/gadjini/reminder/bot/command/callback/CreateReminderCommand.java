package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.service.ReminderService;
import ru.gadjini.reminder.service.ReminderTextBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateReminderCommand implements CallbackBotCommand {

    private static final List<Function<String, ReminderRequest>> REQUEST_EXTRACTORS = new ArrayList<>() {{
        add(new Function<>() {

            Pattern pattern = Pattern.compile("(.*) ((2[0-3]|[01]?[0-9]):([0-5]?[0-9]))");

            @Override
            public ReminderRequest apply(String s) {
                Matcher matcher = pattern.matcher(s);

                if (matcher.matches()) {
                    ReminderRequest reminderRequest = new ReminderRequest();

                    reminderRequest.setText(matcher.group(1).trim());

                    LocalTime localTime = LocalTime.parse(matcher.group(2));
                    LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), localTime);
                    reminderRequest.setRemindAt(localDateTime);

                    return reminderRequest;
                }

                return null;
            }
        });
    }};

    private final ConcurrentHashMap<Long, ReminderRequest> reminderRequests = new ConcurrentHashMap<>();

    private ReminderService reminderService;

    private MessageService messageService;

    private ReminderTextBuilder reminderTextBuilder;

    private String name;

    public CreateReminderCommand(LocalisationService localisationService,
                                 ReminderService reminderService,
                                 MessageService messageService,
                                 ReminderTextBuilder reminderTextBuilder) {
        this.reminderService = reminderService;
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME);
        this.messageService = messageService;
        this.reminderTextBuilder = reminderTextBuilder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        ReminderRequest reminderRequest = new ReminderRequest();

        reminderRequest.setReceiverId(Integer.parseInt(arguments[0]));
        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT);
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        String text = message.getText();
        ReminderRequest candidate = null;

        for (Function<String, ReminderRequest> requestExtractor : REQUEST_EXTRACTORS) {
            candidate = requestExtractor.apply(text);

            if (candidate != null) {
                break;
            }
        }
        if (candidate == null) {
            return;
        }
        ReminderRequest reminderRequest = reminderRequests.get(message.getChatId());
        Reminder reminder = reminderService.createReminder(reminderRequest);
        reminderRequests.remove(message.getChatId());

        String reminderText = reminderTextBuilder.create(reminderRequest.getText(), reminderRequest.getRemindAt());
        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_FROM,
                new Object[]{TgUser.USERNAME_START + message.getFrom().getUserName(), reminderText});
        messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_REMINDER_CREATED,
                new Object[]{reminderText, TgUser.USERNAME_START + reminderRequest.getReceiverName()});
    }
}
