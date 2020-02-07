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
import ru.gadjini.reminder.service.TgUserService;
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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SavedQueryCommand implements KeyboardBotCommand, NavigableBotCommand {

    private Set<String> names = new HashSet<>();

    private CommandStateService stateService;

    private final LocalisationService localisationService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private InlineKeyboardService inlineKeyboardService;

    private SavedQueryService savedQueryService;

    private ReminderRequestExtractor reminderRequestExtractor;

    private SavedQueryMessageBuilder messageBuilder;

    private TgUserService userService;

    @Autowired
    public SavedQueryCommand(LocalisationService localisationService, CommandStateService stateService, MessageService messageService,
                             CurrReplyKeyboard replyKeyboardService, InlineKeyboardService inlineKeyboardService,
                             SavedQueryService savedQueryService, @Qualifier("chain") ReminderRequestExtractor reminderRequestExtractor,
                             SavedQueryMessageBuilder messageBuilder, TgUserService userService) {
        this.localisationService = localisationService;
        this.stateService = stateService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.savedQueryService = savedQueryService;
        this.reminderRequestExtractor = reminderRequestExtractor;
        this.messageBuilder = messageBuilder;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.SAVED_QUERY_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<SavedQuery> queries = savedQueryService.getQueries(message.getFrom().getId());
        Locale locale = userService.getLocale(message.getFrom().getId());
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(message.getChatId())
                        .text(messageBuilder.getMessage(queries, locale))
                        .replyKeyboard(inlineKeyboardService.getSavedQueriesKeyboard(queries.stream().map(SavedQuery::getId).collect(Collectors.toList()))),
                msg -> stateService.setState(msg.getChatId(), msg.getMessageId())
        );
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SAVED_QUERY_INPUT, locale))
                        .replyKeyboard(replyKeyboardService.goBackCommand(message.getChatId(), locale))
        );

        return true;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        reminderRequestExtractor.extract(
                new ReminderRequestContext()
                        .text(text)
                        .user(message.getFrom())
        );
        savedQueryService.saveQuery(message.getFrom().getId(), text);

        List<SavedQuery> queries = savedQueryService.getQueries(message.getFrom().getId());
        int messageId = stateService.getState(message.getChatId(), true);
        Locale locale = userService.getLocale(message.getFrom().getId());
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .messageId(messageId)
                        .text(messageBuilder.getMessage(queries, locale))
                        .replyKeyboard(inlineKeyboardService.getSavedQueriesKeyboard(queries.stream().map(SavedQuery::getId).collect(Collectors.toList())))
        );
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }

    @Override
    public String getHistoryName() {
        return CommandNames.SAVED_QUERY_COMMAND_HISTORY_NAME;
    }
}
