package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.SavedQuery;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestExtractor;
import ru.gadjini.reminder.service.savedquery.SavedQueryMessageBuilder;
import ru.gadjini.reminder.service.savedquery.SavedQueryService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SavedQueryCommand implements KeyboardBotCommand, NavigableBotCommand {

    private String name;

    private CommandStateService stateService;

    private final LocalisationService localisationService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private InlineKeyboardService inlineKeyboardService;

    private SavedQueryService savedQueryService;

    private ReminderRequestExtractor reminderRequestExtractor;

    private SavedQueryMessageBuilder messageBuilder;

    @Autowired
    public SavedQueryCommand(LocalisationService localisationService, CommandStateService stateService, MessageService messageService,
                             CurrReplyKeyboard replyKeyboardService, InlineKeyboardService inlineKeyboardService,
                             SavedQueryService savedQueryService, @Qualifier("chain") ReminderRequestExtractor reminderRequestExtractor,
                             SavedQueryMessageBuilder messageBuilder) {
        name = localisationService.getMessage(MessagesProperties.SAVED_QUERY_COMMAND_NAME);
        this.localisationService = localisationService;
        this.stateService = stateService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.savedQueryService = savedQueryService;
        this.reminderRequestExtractor = reminderRequestExtractor;
        this.messageBuilder = messageBuilder;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<SavedQuery> queries = savedQueryService.getQueries(message.getFrom().getId());
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(message.getChatId())
                        .text(messageBuilder.getMessage(queries))
                        .replyKeyboard(inlineKeyboardService.getSavedQueriesKeyboard(queries.stream().map(SavedQuery::getId).collect(Collectors.toList()))),
                msg -> stateService.setState(msg.getChatId(), msg.getMessageId())
        );

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SAVED_QUERY_INPUT))
                        .replyKeyboard(replyKeyboardService.goBackCommand(message.getChatId()))
        );

        return true;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        reminderRequestExtractor.extract(
                new ReminderRequestContext()
                        .setText(text)
                        .setUser(message.getFrom())
        );
        savedQueryService.saveQuery(message.getFrom().getId(), text);

        List<SavedQuery> queries = savedQueryService.getQueries(message.getFrom().getId());
        int messageId = stateService.getState(message.getChatId(), true);
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .messageId(messageId)
                        .text(messageBuilder.getMessage(queries))
                        .replyKeyboard(inlineKeyboardService.getSavedQueriesKeyboard(queries.stream().map(SavedQuery::getId).collect(Collectors.toList())))
        );
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }

    @Override
    public String getHistoryName() {
        return CommandNames.SAVED_QUERY_COMMAND_HISTORY_NAME;
    }
}
