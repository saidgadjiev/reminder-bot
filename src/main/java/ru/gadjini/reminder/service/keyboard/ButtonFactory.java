package ru.gadjini.reminder.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.Objects;

@Service
public class ButtonFactory {

    private LocalisationService localisationService;

    @Autowired
    public ButtonFactory(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public InlineKeyboardButton deleteCompletedRemindersButton() {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_COMPLETED_REMINDERS_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.DELETE_COMPLETED_REMINDERS_COMMAND_NAME);

        return button;
    }

    public InlineKeyboardButton goBackCallbackButton(String prevHistoryName) {
        Objects.requireNonNull(prevHistoryName);
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_CALLBACK_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams().add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName).serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton goBackCallbackButton(String prevHistoryName, boolean restoreKeyboard, RequestParams requestParams) {
        Objects.requireNonNull(prevHistoryName);
        requestParams.add(Arg.RESTORE_KEYBOARD.getKey(), restoreKeyboard);
        requestParams.add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName);

        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_CALLBACK_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR + requestParams.serialize(CommandParser.COMMAND_ARG_SEPARATOR));
        return button;
    }

    public InlineKeyboardButton completeReminderButton(int reminderId, String currHistoryName) {
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.COMPLETE_REMINDER_COMMAND_DESCRIPTION));
        completeReminderButton.setCallbackData(CommandNames.COMPLETE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .add(Arg.CURR_HISTORY_NAME.getKey(), currHistoryName)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return completeReminderButton;
    }

    public InlineKeyboardButton completeRepeatReminderButton(int reminderId, String currHistoryName) {
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.COMPLETE_REPEAT_REMINDER_COMMAND_DESCRIPTION));
        completeReminderButton.setCallbackData(CommandNames.COMPLETE_REPEAT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .add(Arg.CURR_HISTORY_NAME.getKey(), currHistoryName)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return completeReminderButton;
    }

    public InlineKeyboardButton skipRepeatReminderButton(int reminderId, String currHistoryName) {
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.SKIP_REPEAT_REMINDER_COMMAND_DESCRIPTION));
        completeReminderButton.setCallbackData(CommandNames.SKIP_REPEAT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .add(Arg.CURR_HISTORY_NAME.getKey(), currHistoryName)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return completeReminderButton;
    }

    public InlineKeyboardButton stopRepeatReminderButton(int reminderId, String currHistoryName) {
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.STOP_REPEAT_REMINDER_COMMAND_DESCRIPTION));
        completeReminderButton.setCallbackData(CommandNames.STOP_REPEAT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .add(Arg.CURR_HISTORY_NAME.getKey(), currHistoryName)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return completeReminderButton;
    }

    public InlineKeyboardButton cancelReminderButton(int reminderId, String currHistoryName) {
        InlineKeyboardButton cancelReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION));
        cancelReminderButton.setCallbackData(CommandNames.CANCEL_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .add(Arg.CURR_HISTORY_NAME.getKey(), currHistoryName)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return cancelReminderButton;
    }

    public InlineKeyboardButton customReminderTimeButton(String name, int reminderId, String prevHistoryName) {
        InlineKeyboardButton customRemindButton = new InlineKeyboardButton(name);
        customRemindButton.setCallbackData(CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return customRemindButton;
    }

    public InlineKeyboardButton postponeReminderButton(int reminderId, String prevHistoryName) {
        InlineKeyboardButton postponeButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION));
        postponeButton.setCallbackData(CommandNames.POSTPONE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return postponeButton;
    }

    public InlineKeyboardButton getCompletedRemindersButton() {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.GET_COMPLETED_REMINDERS_COMMAND_NAME);

        return button;
    }

    public InlineKeyboardButton getActiveRemindersButton() {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME);

        return button;
    }

    public InlineKeyboardButton createFriendReminderButton(int friendUserId) {
        InlineKeyboardButton createFriendReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CREATE_FRIEND_REMINDER_COMMAND_DESCRIPTION));
        createFriendReminderButton.setCallbackData(CommandNames.CREATE_FRIEND_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendUserId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return createFriendReminderButton;
    }

    public InlineKeyboardButton deleteFriendButton(int friendUserId) {
        InlineKeyboardButton deleteFriendButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_FRIEND_COMMAND_DESCRIPTION));
        deleteFriendButton.setCallbackData(CommandNames.DELETE_FRIEND_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendUserId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return deleteFriendButton;
    }

    public InlineKeyboardButton acceptFriendRequestButton(int friendUserId) {
        InlineKeyboardButton acceptFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        acceptFriendRequestButton.setCallbackData(CommandNames.ACCEPT_FRIEND_REQUEST_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendUserId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return acceptFriendRequestButton;
    }

    public InlineKeyboardButton rejectFriendRequestButton(int friendUserId) {
        InlineKeyboardButton rejectFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        rejectFriendRequestButton.setCallbackData(CommandNames.REJECT_FRIEND_REQUEST_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendUserId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return rejectFriendRequestButton;
    }

    public InlineKeyboardButton editReminderTimeButton(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.EDIT_REMINDER_TIME_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton editReminderTextButton(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.EDIT_REMINDER_TEXT_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton deleteReminderButton(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.DELETE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton changeReminderNote(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_NOTE_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.EDIT_REMINDER_NOTE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton deleteReminderNote(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_NOTE_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.DELETE_REMINDER_NOTE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton editReminder(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.EDIT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton okButton(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.OK_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.OK_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton reminderTimesScheduleButton(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.SCHEDULE_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.SCHEDULE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton deleteReminderTimeButton(int reminderTimeId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_TIME_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.DELETE_REMINDER_TIME_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_NOTIFICATION_ID.getKey(), reminderTimeId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton changeFriendNameButton(int friendId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CHANGE_FRIEND_NAME_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.CHANGE_FRIEND_NAME_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton paymentButton(String desc, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton(desc);
        button.setUrl(url);

        return button;
    }

    public InlineKeyboardButton returnReminderButton(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.RETURN_REPEAT_REMINDER_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.RETURN_REPEAT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton deactivateReminderButton(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DEACTIVATE_REMINDER_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.DEACTIVATE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton activateReminderButton(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ACTIVATE_REMINDER_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.ACTIVATE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton enableCountSeries(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ENABLE_COUNT_SERIES_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.ENABLE_COUNT_SERIES_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    public InlineKeyboardButton disableCountSeries(int reminderId) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DISABLE_COUNT_SERIES_COMMAND_DESCRIPTION));
        button.setCallbackData(CommandNames.DISABLE_COUNT_SERIES_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }
}
