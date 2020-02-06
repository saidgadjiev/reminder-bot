package ru.gadjini.reminder.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.Locale;
import java.util.Objects;

@Service
public class ButtonFactory {

    private LocalisationService localisationService;

    @Autowired
    public ButtonFactory(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    InlineKeyboardButton deleteCompletedRemindersButton(Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_COMPLETED_REMINDERS_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.DELETE_COMPLETED_REMINDERS_COMMAND_NAME);

        return button;
    }

    public InlineKeyboardButton goBackCallbackButton(String prevHistoryName, RequestParams requestParams, Locale locale) {
        Objects.requireNonNull(prevHistoryName);
        requestParams.add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName);

        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_CALLBACK_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR + requestParams.serialize(CommandParser.COMMAND_ARG_SEPARATOR));
        return button;
    }

    InlineKeyboardButton completeReminderButton(int reminderId, Locale locale) {
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.COMPLETE_REMINDER_COMMAND_DESCRIPTION, locale));
        completeReminderButton.setCallbackData(CommandNames.COMPLETE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return completeReminderButton;
    }

    InlineKeyboardButton completeRepeatReminderButton(int reminderId, Locale locale) {
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.COMPLETE_REPEAT_REMINDER_COMMAND_DESCRIPTION, locale));
        completeReminderButton.setCallbackData(CommandNames.COMPLETE_REPEAT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return completeReminderButton;
    }

    InlineKeyboardButton skipRepeatReminderButton(int reminderId, Locale locale) {
        InlineKeyboardButton skipReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.SKIP_REPEAT_REMINDER_COMMAND_DESCRIPTION, locale));
        skipReminderButton.setCallbackData(CommandNames.SKIP_REPEAT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return skipReminderButton;
    }

    InlineKeyboardButton stopRepeatReminderButton(int reminderId, Locale locale) {
        InlineKeyboardButton stopReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.STOP_REPEAT_REMINDER_COMMAND_DESCRIPTION, locale));
        stopReminderButton.setCallbackData(CommandNames.STOP_REPEAT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return stopReminderButton;
    }

    InlineKeyboardButton cancelReminderButton(int reminderId, Locale locale) {
        InlineKeyboardButton cancelReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION, locale));
        cancelReminderButton.setCallbackData(CommandNames.CANCEL_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return cancelReminderButton;
    }

    InlineKeyboardButton customReminderTimeButton(int reminderId, String prevHistoryName, Locale locale) {
        InlineKeyboardButton customRemindButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION, locale));
        customRemindButton.setCallbackData(CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return customRemindButton;
    }

    InlineKeyboardButton postponeReminderButton(int reminderId, Locale locale) {
        InlineKeyboardButton postponeButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION, locale));
        postponeButton.setCallbackData(CommandNames.POSTPONE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return postponeButton;
    }

    InlineKeyboardButton getCompletedRemindersButton(Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.GET_COMPLETED_REMINDERS_COMMAND_NAME);

        return button;
    }

    InlineKeyboardButton getActiveRemindersButton(String nameCode, ReminderDao.Filter filter, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(nameCode, locale));
        button.setCallbackData(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FILTER.getKey(), filter.getCode()).serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton createFriendReminderButton(int friendUserId, Locale locale) {
        InlineKeyboardButton createFriendReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CREATE_FRIEND_REMINDER_COMMAND_DESCRIPTION, locale));
        createFriendReminderButton.setCallbackData(CommandNames.CREATE_FRIEND_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendUserId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return createFriendReminderButton;
    }

    InlineKeyboardButton deleteFriendButton(int friendUserId, Locale locale) {
        InlineKeyboardButton deleteFriendButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_FRIEND_COMMAND_DESCRIPTION, locale));
        deleteFriendButton.setCallbackData(CommandNames.DELETE_FRIEND_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendUserId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return deleteFriendButton;
    }

    InlineKeyboardButton acceptFriendRequestButton(int friendUserId, Locale locale) {
        InlineKeyboardButton acceptFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_DESCRIPTION, locale));
        acceptFriendRequestButton.setCallbackData(CommandNames.ACCEPT_FRIEND_REQUEST_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendUserId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return acceptFriendRequestButton;
    }

    InlineKeyboardButton rejectFriendRequestButton(int friendUserId, Locale locale) {
        InlineKeyboardButton rejectFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_DESCRIPTION, locale));
        rejectFriendRequestButton.setCallbackData(CommandNames.REJECT_FRIEND_REQUEST_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendUserId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return rejectFriendRequestButton;
    }

    InlineKeyboardButton editReminderTimeButton(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.EDIT_REMINDER_TIME_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton editReminderTextButton(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.EDIT_REMINDER_TEXT_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton deleteReminderButton(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.DELETE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton changeReminderNote(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_NOTE_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.EDIT_REMINDER_NOTE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton deleteReminderNote(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_NOTE_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.DELETE_REMINDER_NOTE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton editReminder(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.EDIT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton reminderDetails(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REMINDER_DETAILS_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.REMINDER_DETAILS_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton okButton(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.OK_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.OK_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton reminderTimesScheduleButton(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.SCHEDULE_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.SCHEDULE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton deleteReminderTimeButton(int reminderTimeId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_TIME_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.DELETE_REMINDER_TIME_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_NOTIFICATION_ID.getKey(), reminderTimeId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton changeFriendNameButton(int friendId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CHANGE_FRIEND_NAME_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.CHANGE_FRIEND_NAME_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.FRIEND_ID.getKey(), friendId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton paymentButton(String desc, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton(desc);
        button.setUrl(url);

        return button;
    }

    InlineKeyboardButton returnReminderButton(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.RETURN_REPEAT_REMINDER_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.RETURN_REPEAT_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton deactivateReminderButton(int reminderId, RequestParams requestParams, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DEACTIVATE_REMINDER_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.DEACTIVATE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .merge(requestParams)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton activateReminderButton(int reminderId, RequestParams requestParams, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ACTIVATE_REMINDER_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.ACTIVATE_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .merge(requestParams)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton enableCountSeries(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ENABLE_COUNT_SERIES_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.ENABLE_COUNT_SERIES_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton disableCountSeries(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DISABLE_COUNT_SERIES_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.DISABLE_COUNT_SERIES_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton readButton(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.READ_REMINDER_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.READ_REMINDER_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.REMINDER_ID.getKey(), reminderId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton delegateButton(String name, String delegate, RequestParams requestParams) {
        requestParams.add(Arg.CALLBACK_DELEGATE.getKey(), delegate);

        InlineKeyboardButton button = new InlineKeyboardButton(name);
        button.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                requestParams.serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }

    InlineKeyboardButton suppressNotifications(int reminderId, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.SUPPRESS_NOTIFICATIONS_COMMAND_DESCRIPTION, locale));
        button.setCallbackData(CommandNames.SUPPRESS_NOTIFICATIONS_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId).serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return button;
    }
}
