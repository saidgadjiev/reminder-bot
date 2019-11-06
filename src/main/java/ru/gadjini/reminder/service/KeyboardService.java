package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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

    public InlineKeyboardMarkup getReminderButtons(int reminderId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REMINDER_COMPLETE_COMMAND_DESCRIPTION));
        completeReminderButton.setCallbackData(localisationService.getMessage(MessagesProperties.REMINDER_COMPLETE_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + reminderId);
        row1.add(completeReminderButton);
        InlineKeyboardButton cancelReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION));
        cancelReminderButton.setCallbackData(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + reminderId);
        row1.add(cancelReminderButton);

        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton customRemindButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CUSTOM_REMIND_COMMAND_DESCRIPTION));
        customRemindButton.setCallbackData(MessagesProperties.CUSTOM_REMIND_COMMAND_NAME + CommandRegistry.COMMAND_ARG_SEPARATOR + reminderId);
        row2.add(customRemindButton);
        InlineKeyboardButton postponeButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION));
        postponeButton.setCallbackData(localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + reminderId);
        row2.add(postponeButton);

        keyboard.add(row2);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_DESCRIPTION));
        inlineKeyboardButton.setCallbackData(localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + friendUserId);
        row.add(inlineKeyboardButton);

        InlineKeyboardButton deleteFriendButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DELETE_FRIEND_COMMAND_DESCRIPTION));
        deleteFriendButton.setCallbackData(localisationService.getMessage(MessagesProperties.DELETE_FRIEND_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + friendUserId);
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
        acceptFriendRequestButton.setCallbackData(localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + friendUserId);
        row.add(acceptFriendRequestButton);

        InlineKeyboardButton rejectFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        rejectFriendRequestButton.setCallbackData(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + friendUserId);
        row.add(rejectFriendRequestButton);

        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.COMMAND_GET_REMINDERS_NAME));
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

    public ReplyKeyboardMarkup goBackCommand() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME));
        }});

        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup reminderKeyboard(int reminderId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        row1.add(new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_DESCRIPTION));
            setCallbackData(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + reminderId);
        }});
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();

        row2.add(new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_DESCRIPTION));
            setCallbackData(localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TEXT_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + reminderId);
        }});
        keyboard.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();

        row3.add(new InlineKeyboardButton() {{
            setText(localisationService.getMessage(MessagesProperties.DELETE_FRIEND_COMMAND_DESCRIPTION));
            setCallbackData(localisationService.getMessage(MessagesProperties.DELETE_REMINDER_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + reminderId);
        }});
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup replyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setKeyboard(new ArrayList<>());
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }
}
