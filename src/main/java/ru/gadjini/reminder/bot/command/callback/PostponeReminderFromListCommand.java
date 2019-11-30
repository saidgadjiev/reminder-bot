package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.ChangeReminderRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;

import java.util.concurrent.ConcurrentHashMap;

public class PostponeReminderFromListCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, ChangeReminderRequest> reminderRequests = new ConcurrentHashMap<>();

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderRequestService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    public PostponeReminderFromListCommand(MessageService messageService,
                                           KeyboardService keyboardService,
                                           ReminderRequestService reminderService,
                                           ReminderMessageSender reminderMessageSender,
                                           CommandNavigator commandNavigator, LocalisationService localisationService) {
        this.localisationService = localisationService;
        this.name = MessagesProperties.POSTPONE_REMINDER_FROM_LIST_COMMAND_NAME;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        reminderRequests.put(callbackQuery.getMessage().getChatId(), new ChangeReminderRequest() {{
            setMessageId(callbackQuery.getMessage().getMessageId());
            setReminderId(Integer.parseInt(arguments[0]));
        }});

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getMessage().getText() + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_TIME),
                keyboardService.goBackCallbackCommand(MessagesProperties.REMINDER_DETAILS_COMMAND_NAME, new String[]{arguments[0], String.valueOf(true)})
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION);
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        ChangeReminderRequest changeReminderRequest = reminderRequests.get(message.getChatId());
        UpdateReminderResult updateReminderResult = reminderService.postponeReminder(changeReminderRequest.getReminderId(), message.getText().trim());
        updateReminderResult.getOldReminder().getReceiver().setChatId(message.getChatId());
        reminderRequests.remove(message.getChatId());

        ReplyKeyboardMarkup replyKeyboard = commandNavigator.silentPop(message.getChatId());

        reminderMessageSender.sendReminderPostponedFromList(message.getChatId(), changeReminderRequest.getMessageId(), updateReminderResult, replyKeyboard);
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }
}
