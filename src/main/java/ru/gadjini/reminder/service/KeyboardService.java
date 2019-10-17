package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
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
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton completeReminderButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REMINDER_COMPLETE_COMMAND_NAME));
        completeReminderButton.setCallbackData(MessagesProperties.COMPLETE_REMINDER_COMMAND_NAME + CommandRegistry.COMMAND_ARG_SEPARATOR + reminderId);

        row.add(completeReminderButton);
        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendKeyboard(int friendId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_DESCRIPTION));
        inlineKeyboardButton.setCallbackData(BotCommand.COMMAND_INIT_CHARACTER + localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + friendId);
        row.add(inlineKeyboardButton);

        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendRequestKeyboard(int senderId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton acceptFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        acceptFriendRequestButton.setCallbackData(localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + senderId);
        row.add(acceptFriendRequestButton);

        InlineKeyboardButton rejectFriendRequestButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_DESCRIPTION));
        rejectFriendRequestButton.setCallbackData(localisationService.getMessage(MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_NAME) + CommandRegistry.COMMAND_ARG_SEPARATOR + senderId);
        row.add(rejectFriendRequestButton);

        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GET_FRIENDS_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GET_FRIEND_REQUESTS_COMMAND_NAME));
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
