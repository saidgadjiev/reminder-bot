package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
}
