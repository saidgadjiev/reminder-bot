package ru.gadjini.reminder.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class KeyboardUtils {

    private KeyboardUtils() {
    }

    public static InlineKeyboardMarkup removeButton(InlineKeyboardMarkup keyboardMarkup, String buttonName) {
        for (List<InlineKeyboardButton> keyboardButtons : keyboardMarkup.getKeyboard()) {
            boolean removed = keyboardButtons.removeIf(inlineKeyboardButton -> {
                return inlineKeyboardButton.getCallbackData().startsWith(buttonName);
            });

            if (removed) {
                return keyboardMarkup;
            }
        }

        return keyboardMarkup;
    }
}
