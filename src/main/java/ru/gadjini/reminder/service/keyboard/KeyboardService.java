package ru.gadjini.reminder.service.keyboard;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.CommandExecutor;
import ru.gadjini.reminder.service.LocalisationService;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardService {

    private LocalisationService localisationService;

    private ButtonFactory buttonFactory;

    @Autowired
    public KeyboardService(LocalisationService localisationService, ButtonFactory buttonFactory) {
        this.localisationService = localisationService;
        this.buttonFactory = buttonFactory;
    }

    public InlineKeyboardMarkup getActiveRemindersListKeyboard(List<Integer> reminderIds, String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(reminderIds, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int remindId : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(MessagesProperties.REMINDER_DETAILS_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + remindId);
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }
        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getCompletedRemindersListKeyboard(String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteCompletedRemindersButton()));
        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getEmptyRemindersListKeyboard(String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReceiverReminderDetailsKeyboard(int reminderId, String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminderId), buttonFactory.cancelReminderButton(reminderId)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(reminderId), buttonFactory.postponeReminderButton(reminderId)));

        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRemindersMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getCompletedRemindersButton()));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton()));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.createFriendReminderButton(friendUserId),
                buttonFactory.deleteFriendButton(friendUserId))
        );

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendRequestKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.acceptFriendRequestButton(friendUserId),
                buttonFactory.rejectFriendRequestButton(friendUserId)
        ));

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GET_REMINDERS_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GET_FRIENDS_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GET_FRIEND_REQUESTS_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.SEND_FRIEND_REQUEST_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.CHANGE_TIMEZONE_COMMAND_NAME));
        }});

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup goBackCommand() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME));
        }});

        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackCommand(String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getCreatorReminderDetailsKeyboard(int reminderId, String prevHistoryName) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderButton(reminderId)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getMySelfReminderDetailsKeyboard(int reminderId, String prevHistoryName) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminderId), buttonFactory.cancelReminderButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(reminderId), buttonFactory.postponeReminderButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminder(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderButton(reminderId)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderDetailsKeyboard(int currUserId, Reminder reminder) {
        if (reminder.getCreatorId() == reminder.getReceiverId()) {
            return getMySelfReminderDetailsKeyboard(reminder.getId(), MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME);
        } else if (currUserId == reminder.getReceiverId()) {
            return getReceiverReminderDetailsKeyboard(reminder.getId(), MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME);
        } else {
            return getCreatorReminderDetailsKeyboard(reminder.getId(), MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME);
        }
    }

    public InlineKeyboardMarkup getEditReminderKeyboard(int reminderId, String prevHistoryName) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTimeButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTextButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.changeReminderNote(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderNote(reminderId)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, new String[]{String.valueOf(reminderId)})));
        }

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup replyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setKeyboard(new ArrayList<>());
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup inlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());

        return inlineKeyboardMarkup;
    }
}
