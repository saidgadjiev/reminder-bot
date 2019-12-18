package ru.gadjini.reminder.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.ArrayList;

@Service
public class ReplyKeyboardService {

    private LocalisationService localisationService;

    @Autowired
    public ReplyKeyboardService(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public ReplyKeyboardMarkup getUserSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(
                new KeyboardRow() {{
                    add(new KeyboardButton(localisationService.getMessage(MessagesProperties.CHANGE_TIMEZONE_COMMAND_NAME)));
                }}
        );
        replyKeyboardMarkup.getKeyboard().add(
                new KeyboardRow() {{
                    add(new KeyboardButton(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_COMMAND_NAME)));
                }}
        );
        replyKeyboardMarkup.getKeyboard().add(
                new KeyboardRow() {{
                    add(new KeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME)));
                }}
        );

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getUserReminderNotificationSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITH_TIME_COMMAND_NAME)));
        }});
        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_COMMAND_NAME)));
        }});
        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME)));
        }});

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getPostponeMessagesKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_REASON_MEETING)));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON)));
        }});

        return replyKeyboardMarkup;
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
            add(localisationService.getMessage(MessagesProperties.USER_SETTINGS_COMMAND_NAME));
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

    public ReplyKeyboardMarkup postponeTimeKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_15_MIN)));
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_30_MIN)));
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
