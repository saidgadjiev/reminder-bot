package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.SavedQuery;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.savedquery.SavedQueryMessageBuilder;
import ru.gadjini.reminder.service.savedquery.SavedQueryService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeleteSavedQueryCommand implements CallbackBotCommand {

    private SavedQueryService savedQueryService;

    private MessageService messageService;

    private SavedQueryMessageBuilder messageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public DeleteSavedQueryCommand(SavedQueryService savedQueryService, MessageService messageService, SavedQueryMessageBuilder messageBuilder, InlineKeyboardService inlineKeyboardService) {
        this.savedQueryService = savedQueryService;
        this.messageService = messageService;
        this.messageBuilder = messageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public String getName() {
        return CommandNames.DELETE_SAVED_QUERY_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int queryId = requestParams.getInt(Arg.SAVED_QUERY_ID.getKey());
        savedQueryService.delete(queryId);

        List<SavedQuery> queries = savedQueryService.getQueries(callbackQuery.getFrom().getId());
        messageService.editMessage(
                new EditMessageContext()
                        .chatId(callbackQuery.getMessage().getChatId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(messageBuilder.getMessage(queries))
                        .replyKeyboard(inlineKeyboardService.getSavedQueriesKeyboard(queries.stream().map(SavedQuery::getId).collect(Collectors.toList())))
        );

        return null;
    }
}
