package ru.gadjini.reminder.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gadjini.reminder.common.MessagesProperties;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardService {

    private LocalisationService localisationService;

    @Autowired
    public KeyboardService(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public InlineKeyboardMarkup getReminderListButtons(List<Integer> reminderIds, String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(reminderIds, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int remindId : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(MessagesProperties.EDIT_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + remindId);
                row.add(button);
            }

            keyboard.add(row);
        }
        if (prevHistoryName != null) {
            keyboard.add(new ArrayList<>() {{
                add(new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_CALLBACK_COMMAND_DESCRIPTION)) {{
                    setCallbackData(MessagesProperties.GO_BACK_CALLBACK_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + prevHistoryName);
                }});
            }});
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderButtons(int reminderId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REMINDER_COMPLETE_COMMAND_DESCRIPTION));
        completeReminderButton.setCallbackData(MessagesProperties.REMINDER_COMPLETE_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        row1.add(completeReminderButton);
        InlineKeyboardButton cancelReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION));
        cancelReminderButton.setCallbackData(MessagesProperties.CANCEL_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        row1.add(cancelReminderButton);

        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton customRemindButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CUSTOM_REMIND_COMMAND_DESCRIPTION));
        customRemindButton.setCallbackData(MessagesProperties.CUSTOM_REMIND_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        row2.add(customRemindButton);
        InlineKeyboardButton postponeButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION));
        postponeButton.setCallbackData(MessagesProperties.POSTPONE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        row2.add(postponeButton);

        keyboard.add(row2);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRemindersMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(new ArrayList<>() {{
            add(new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_DESCRIPTION)) {{
                setCallbackData(MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_NAME);
            }});
        }});
        keyboard.add(new ArrayList<>() {{
            add(new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_DESCRIPTION)) {{
                setCallbackData(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME);
            }});
        }});
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_DESCRIPTION));
        inlineKeyboardButton.setCallbackData(MessagesProperties.CREATE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + friendUserId);
        row.add(inlineKeyboardButton);

        InlineKeyboardButton deleteFriendButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_FRIEND_COMMAND_DESCRIPTION));
        deleteFriendButton.setCallbackData(MessagesProperties.DELETE_FRIEND_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + friendUserId);
        row.add(deleteFriendButton);

        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendRequestKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton acceptFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        acceptFriendRequestButton.setCallbackData(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + friendUserId);
        row.add(acceptFriendRequestButton);

        InlineKeyboardButton rejectFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        rejectFriendRequestButton.setCallbackData(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + friendUserId);
        row.add(rejectFriendRequestButton);

        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

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

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup goBackCallbackCommand() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME));
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

    public InlineKeyboardMarkup getChangeReminderKeyboard(int reminderId, String prevHistoryName) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        row1.add(new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }});
        row1.add(new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }});
        row1.add(new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.DELETE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }});
        keyboard.add(row1);

        if (prevHistoryName != null) {
            List<InlineKeyboardButton> row2 = new ArrayList<>();

            row2.add(new InlineKeyboardButton() {{
                setText(localisationService.getMessage(MessagesProperties.GO_BACK_CALLBACK_COMMAND_DESCRIPTION));
                setCallbackData(MessagesProperties.GO_BACK_CALLBACK_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + prevHistoryName);
            }});
            keyboard.add(row2);
        }

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup completedReminderKeyboard(int reminderId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_COMMAND_DESCRIPTION));
            setCallbackData(MessagesProperties.DELETE_REMINDER_COMMAND_NAME + CommandExecutor.COMMAND_ARG_SEPARATOR + reminderId);
        }});
        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup completedReminderKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.REMOVE_ALL_NON_COMMAND_UPDATE)));
        }});
        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME));
        }});

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup replyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setKeyboard(new ArrayList<>());
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }
}
