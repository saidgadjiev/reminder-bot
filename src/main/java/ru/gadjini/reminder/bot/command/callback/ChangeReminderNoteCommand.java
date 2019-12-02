package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.ChangeReminderRequest;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;

import java.util.concurrent.ConcurrentHashMap;

public class ChangeReminderNoteCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, ChangeReminderRequest> changeReminderTimeRequests = new ConcurrentHashMap<>();

    private String name;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderService reminderService;

    private CommandNavigator commandNavigator;

    private KeyboardService keyboardService;

    private final LocalisationService localisationService;

    public ChangeReminderNoteCommand(ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderService reminderService,
                                     CommandNavigator commandNavigator,
                                     KeyboardService keyboardService,
                                     LocalisationService localisationService) {
        this.keyboardService = keyboardService;
        this.localisationService = localisationService;
        this.name = MessagesProperties.EDIT_REMINDER_NOTE_COMMAND_NAME;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.commandNavigator = commandNavigator;
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
                callbackQuery.getMessage().getText() + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_EDIT_NOTE),
                keyboardService.goBackCallbackButton(MessagesProperties.EDIT_REMINDER_COMMAND_NAME, true, new String[]{arguments[0]})
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_REMINDER_NOTE_ANSWER);
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        String text = message.getText().trim();

        ChangeReminderRequest request = changeReminderTimeRequests.get(message.getChatId());
        Reminder reminder = reminderService.changeReminderNote(request.getReminderId(), text);
        reminder.getCreator().setChatId(message.getChatId());

        ReplyKeyboard replyKeyboard = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderNoteChanged(reminder, request.getMessageId(), replyKeyboard);
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }

    @Override
    public String getHistoryName() {
        return name;
    }
}
