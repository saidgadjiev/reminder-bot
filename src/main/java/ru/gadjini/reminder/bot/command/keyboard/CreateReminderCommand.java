package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.savedquery.SavedQueryService;

import java.util.List;

@Component
public class CreateReminderCommand implements KeyboardBotCommand {

    private String name;

    private final LocalisationService localisationService;

    private SavedQueryService savedQueryService;

    private ReplyKeyboardService replyKeyboardService;

    private MessageService messageService;

    @Autowired
    public CreateReminderCommand(LocalisationService localisationService, SavedQueryService savedQueryService,
                                 CurrReplyKeyboard replyKeyboardService, MessageService messageService) {
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME);
        this.localisationService = localisationService;
        this.savedQueryService = savedQueryService;
        this.replyKeyboardService = replyKeyboardService;
        this.messageService = messageService;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<String> queries = savedQueryService.getQueriesOnly(message.getFrom().getId());
        ReplyKeyboardMarkup savedQueriesKeyboard = replyKeyboardService.getSavedQueriesKeyboard(message.getChatId(), queries);

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CREATE_REMINDER))
                        .replyKeyboard(savedQueriesKeyboard)
        );

        return false;
    }
}
