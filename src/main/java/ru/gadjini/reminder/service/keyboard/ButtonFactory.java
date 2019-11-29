package ru.gadjini.reminder.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.command.CommandExecutor;
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
        return new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_COMPLETED_REMINDERS_COMMAND_DESCRIPTION)) {{
            setCallbackData(MessagesProperties.DELETE_COMPLETED_REMINDERS_COMMAND_NAME);
        }};
    }

    public InlineKeyboardButton goBackCallbackButton(String prevHistoryName) {
        Objects.requireNonNull(prevHistoryName);
        return new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_CALLBACK_COMMAND_DESCRIPTION)) {{
            setCallbackData(MessagesProperties.GO_BACK_CALLBACK_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + prevHistoryName);
        }};
    }

    public InlineKeyboardButton goBackCallbackButton(String prevHistoryName, String[] arguments) {
        Objects.requireNonNull(prevHistoryName);
        return new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_CALLBACK_COMMAND_DESCRIPTION)) {{
            setCallbackData(MessagesProperties.GO_BACK_CALLBACK_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + prevHistoryName
                    + CommandExecutor.COMMAND_ARG_SEPARATOR + String.join(CommandExecutor.COMMAND_ARG_SEPARATOR, arguments));
        }};
    }

    public InlineKeyboardButton completeReminderButton(int reminderId) {
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.COMPLETE_REMINDER_COMMAND_DESCRIPTION));
        completeReminderButton.setCallbackData(MessagesProperties.COMPLETE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);

        return completeReminderButton;
    }

    public InlineKeyboardButton cancelReminderButton(int reminderId) {
        InlineKeyboardButton cancelReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION));
        cancelReminderButton.setCallbackData(MessagesProperties.CANCEL_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);

        return cancelReminderButton;
    }

    public InlineKeyboardButton customReminderTimeButton(int reminderId) {
        InlineKeyboardButton customRemindButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION));
        customRemindButton.setCallbackData(MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);

        return customRemindButton;
    }

    public InlineKeyboardButton postponeReminderButton(int reminderId) {
        InlineKeyboardButton postponeButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION));
        postponeButton.setCallbackData(MessagesProperties.POSTPONE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);

        return postponeButton;
    }

    public InlineKeyboardButton getCompletedRemindersButton() {
        return new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_DESCRIPTION)) {{
            setCallbackData(MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_NAME);
        }};
    }

    public InlineKeyboardButton getActiveRemindersButton() {
        return new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_DESCRIPTION)) {{
            setCallbackData(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME);
        }};
    }

    public InlineKeyboardButton createFriendReminderButton(int friendUserId) {
        InlineKeyboardButton createFriendReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_DESCRIPTION));
        createFriendReminderButton.setCallbackData(MessagesProperties.CREATE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + friendUserId);

        return createFriendReminderButton;
    }

    public InlineKeyboardButton deleteFriendButton(int friendUserId) {
        InlineKeyboardButton deleteFriendButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_FRIEND_COMMAND_DESCRIPTION));
        deleteFriendButton.setCallbackData(MessagesProperties.DELETE_FRIEND_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + friendUserId);

        return deleteFriendButton;
    }

    public InlineKeyboardButton acceptFriendRequestButton(int friendUserId) {
        InlineKeyboardButton acceptFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        acceptFriendRequestButton.setCallbackData(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + friendUserId);

        return acceptFriendRequestButton;
    }

    public InlineKeyboardButton rejectFriendRequestButton(int friendUserId) {
        InlineKeyboardButton rejectFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        rejectFriendRequestButton.setCallbackData(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + friendUserId);

        return rejectFriendRequestButton;
    }

    public InlineKeyboardButton editReminderTimeButton(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }};
    }

    public InlineKeyboardButton editReminderTextButton(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }};
    }

    public InlineKeyboardButton deleteReminderButton(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.DELETE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }};
    }

    public InlineKeyboardButton changeReminderNote(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_NOTE_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_NOTE_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }};
    }

    public InlineKeyboardButton deleteReminderNote(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_NOTE_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.DELETE_REMINDER_NOTE_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }};
    }

    public InlineKeyboardButton editReminder(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }};
    }
}
