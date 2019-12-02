package ru.gadjini.reminder.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
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
            setCallbackData(MessagesProperties.GO_BACK_CALLBACK_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName);
            }}.serialize("="));
        }};
    }

    public InlineKeyboardButton goBackCallbackButton(String prevHistoryName, boolean restoreKeyboard, RequestParams requestParams) {
        Objects.requireNonNull(prevHistoryName);
        requestParams.add(Arg.RESTORE_KEYBOARD.getKey(), restoreKeyboard);
        requestParams.add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName);

        return new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_CALLBACK_COMMAND_DESCRIPTION)) {{
            setCallbackData(MessagesProperties.GO_BACK_CALLBACK_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + requestParams.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
        }};
    }

    public InlineKeyboardButton completeReminderButton(int reminderId, String prevHistoryName) {
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.COMPLETE_REMINDER_COMMAND_DESCRIPTION));
        completeReminderButton.setCallbackData(MessagesProperties.COMPLETE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
           add(Arg.REMINDER_ID.getKey(), reminderId);
           add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName);
        }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));

        return completeReminderButton;
    }

    public InlineKeyboardButton cancelReminderButton(int reminderId, String prevHistoryName) {
        InlineKeyboardButton cancelReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION));
        cancelReminderButton.setCallbackData(MessagesProperties.CANCEL_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
            add(Arg.REMINDER_ID.getKey(), reminderId);
            add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName);
        }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));

        return cancelReminderButton;
    }

    public InlineKeyboardButton customReminderTimeButton(int reminderId, String prevHistoryName) {
        InlineKeyboardButton customRemindButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION));
        customRemindButton.setCallbackData(MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
            add(Arg.REMINDER_ID.getKey(), reminderId);
            add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName);
        }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));

        return customRemindButton;
    }

    public InlineKeyboardButton postponeReminderButton(int reminderId, String prevHistoryName) {
        InlineKeyboardButton postponeButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION));
        postponeButton.setCallbackData(MessagesProperties.POSTPONE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
            add(Arg.REMINDER_ID.getKey(), reminderId);
            add(Arg.PREV_HISTORY_NAME.getKey(), prevHistoryName);
        }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));

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
        createFriendReminderButton.setCallbackData(MessagesProperties.CREATE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
            add(Arg.FRIEND_ID.getKey(), friendUserId);
        }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));

        return createFriendReminderButton;
    }

    public InlineKeyboardButton deleteFriendButton(int friendUserId) {
        InlineKeyboardButton deleteFriendButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_FRIEND_COMMAND_DESCRIPTION));
        deleteFriendButton.setCallbackData(MessagesProperties.DELETE_FRIEND_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
            add(Arg.FRIEND_ID.getKey(), friendUserId);
        }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));

        return deleteFriendButton;
    }

    public InlineKeyboardButton acceptFriendRequestButton(int friendUserId) {
        InlineKeyboardButton acceptFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        acceptFriendRequestButton.setCallbackData(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
            add(Arg.FRIEND_ID.getKey(), friendUserId);
        }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));

        return acceptFriendRequestButton;
    }

    public InlineKeyboardButton rejectFriendRequestButton(int friendUserId) {
        InlineKeyboardButton rejectFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        rejectFriendRequestButton.setCallbackData(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
            add(Arg.FRIEND_ID.getKey(), friendUserId);
        }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));

        return rejectFriendRequestButton;
    }

    public InlineKeyboardButton editReminderTimeButton(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                add(Arg.REMINDER_ID.getKey(), reminderId);
            }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
        }};
    }

    public InlineKeyboardButton editReminderTextButton(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                add(Arg.REMINDER_ID.getKey(), reminderId);
            }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
        }};
    }

    public InlineKeyboardButton deleteReminderButton(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.DELETE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                add(Arg.REMINDER_ID.getKey(), reminderId);
            }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
        }};
    }

    public InlineKeyboardButton changeReminderNote(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_NOTE_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_NOTE_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                add(Arg.REMINDER_ID.getKey(), reminderId);
            }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
        }};
    }

    public InlineKeyboardButton deleteReminderNote(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_NOTE_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.DELETE_REMINDER_NOTE_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                add(Arg.REMINDER_ID.getKey(), reminderId);
            }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
        }};
    }

    public InlineKeyboardButton editReminder(int reminderId) {
        return new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                add(Arg.REMINDER_ID.getKey(), reminderId);
            }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
        }};
    }
}
