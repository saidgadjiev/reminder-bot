package ru.gadjini.reminder.service.keyboard.reply;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@Qualifier("keyboard")
public class ReplyKeyboardServiceImpl implements ReplyKeyboardService {

    private LocalisationService localisationService;

    @Autowired
    public ReplyKeyboardServiceImpl(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ReplyKeyboardMarkup getSavedQueriesKeyboard(long chatId, List<String> queries, Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        if (queries.isEmpty()) {
            replyKeyboardMarkup.setResizeKeyboard(true);
        }

        List<KeyboardRow> keyboard = replyKeyboardMarkup.getKeyboard();
        queries.forEach(s -> keyboard.add(keyboardRow(s)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    @Override
    public ReplyKeyboardMarkup getFriendRequestsKeyboard(long chatId, Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = replyKeyboardMarkup.getKeyboard();
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.TO_ME_FRIEND_REQUESTS_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.FROM_ME_FRIEND_REQUESTS_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    @Override
    public ReplyKeyboardMarkup getUserSettingsKeyboard(long chatId, Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        List<KeyboardRow> keyboard = replyKeyboardMarkup.getKeyboard();
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.CHANGE_TIMEZONE_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.REFRESH_USER_DATA_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    @Override
    public ReplyKeyboard removeKeyboard(long chatId) {
        return new ReplyKeyboardRemove();
    }

    @Override
    public ReplyKeyboardMarkup getUserReminderNotificationSettingsKeyboard(long chatId, Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        List<KeyboardRow> keyboard = replyKeyboardMarkup.getKeyboard();
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITH_TIME_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.MAIN_MENU_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    @Override
    public ReplyKeyboardMarkup getMainMenu(long chatId, Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        List<KeyboardRow> keyboard = replyKeyboardMarkup.getKeyboard();
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME, locale), localisationService.getMessage(MessagesProperties.GET_REMINDERS_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.CREATE_CHALLENGE_COMMAND_NAME, locale), localisationService.getMessage(MessagesProperties.GET_CHALLENGES_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.GOALS_COMMAND_DESCRIPTION, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.GET_FRIENDS_COMMAND_NAME, locale), localisationService.getMessage(MessagesProperties.FRIEND_REQUESTS_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.SEND_FRIEND_REQUEST_COMMAND_NAME, locale), localisationService.getMessage(MessagesProperties.SAVED_QUERY_COMMAND_NAME, locale)));
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.USER_SETTINGS_COMMAND_NAME, locale), localisationService.getMessage(MessagesProperties.HELP_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    @Override
    public ReplyKeyboardMarkup goBackCommand(long chatId, Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        List<KeyboardRow> keyboard = replyKeyboardMarkup.getKeyboard();
        keyboard.add(keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup replyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setKeyboard(new ArrayList<>());
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    private KeyboardRow keyboardRow(String... buttons) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.addAll(Arrays.asList(buttons));

        return keyboardRow;
    }
}
