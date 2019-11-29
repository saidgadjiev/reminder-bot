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
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;

import java.util.concurrent.ConcurrentHashMap;

public class PostponeReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, ChangeReminderRequest> reminderRequests = new ConcurrentHashMap<>();

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderRequestService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    public PostponeReminderCommand(MessageService messageService,
                                   KeyboardService keyboardService,
                                   ReminderRequestService reminderService,
                                   ReminderMessageSender reminderMessageSender,
                                   CommandNavigator commandNavigator) {
        this.name = MessagesProperties.POSTPONE_REMINDER_COMMAND_NAME;
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
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION);
        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_POSTPONE_TIME, keyboardService.goBackCommand());
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    /**
     * Принимает команды: на 1д 1ч 1мин, до завтра 15:00;
     */
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

        reminderMessageSender.sendReminderPostponed(updateReminderResult, replyKeyboard);
    }
}
