package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
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

public class ChangeReminderTimeCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, ChangeReminderRequest> changeReminderTimeRequests = new ConcurrentHashMap<>();

    private String name;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderRequestService reminderService;

    private CommandNavigator commandNavigator;

    private KeyboardService keyboardService;

    private LocalisationService localisationService;

    public ChangeReminderTimeCommand(ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderRequestService reminderService,
                                     CommandNavigator commandNavigator,
                                     KeyboardService keyboardService,
                                     LocalisationService localisationService) {
        this.localisationService = localisationService;
        this.name = MessagesProperties.EDIT_REMINDER_TIME_COMMAND_NAME;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.commandNavigator = commandNavigator;
        this.keyboardService = keyboardService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        changeReminderTimeRequests.put(callbackQuery.getMessage().getChatId(), new ChangeReminderRequest() {{
            setReminderId(Integer.parseInt(arguments[0]));
            setMessageId(callbackQuery.getMessage().getMessageId());
        }});

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getMessage().getText() + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME),
                keyboardService.goBackCallbackCommand(MessagesProperties.EDIT_REMINDER_COMMAND_NAME, new String[]{arguments[0]})
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_REMINDER_TIME_ANSWER);
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
        ChangeReminderRequest request = changeReminderTimeRequests.get(message.getChatId());
        UpdateReminderResult updateReminderResult = reminderService.changeReminderTime(request.getReminderId(), message.getText().trim());
        updateReminderResult.getOldReminder().getCreator().setChatId(message.getChatId());

        ReplyKeyboard replyKeyboard = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderTimeChanged(request.getMessageId(), updateReminderResult, replyKeyboard);
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }
}
