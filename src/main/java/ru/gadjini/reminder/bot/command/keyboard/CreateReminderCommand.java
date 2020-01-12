package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.suggestion.SuggestionService;

import java.util.List;

@Component
public class CreateReminderCommand implements KeyboardBotCommand {

    private String name;

    private final LocalisationService localisationService;

    private SuggestionService suggestionService;

    private ReplyKeyboardService replyKeyboardService;

    private MessageService messageService;

    @Autowired
    public CreateReminderCommand(LocalisationService localisationService, SuggestionService suggestionService,
                                 ReplyKeyboardService replyKeyboardService, MessageService messageService) {
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME);
        this.localisationService = localisationService;
        this.suggestionService = suggestionService;
        this.replyKeyboardService = replyKeyboardService;
        this.messageService = messageService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<String> suggestions = suggestionService.getSuggestions(message.getFrom().getId());
        ReplyKeyboardMarkup suggestionsKeyboard = replyKeyboardService.getSuggestionsKeyboard(suggestions);

        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CREATE_REMINDER))
                        .replyKeyboard(suggestionsKeyboard)
        );

        return false;
    }
}
