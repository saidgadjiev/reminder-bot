package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;
import ru.gadjini.reminder.service.savedquery.SavedQueryService;

import java.util.List;

@Component
public class CreateReminderCommand implements KeyboardBotCommand, NavigableBotCommand {

    private String name;

    private final LocalisationService localisationService;

    private SavedQueryService savedQueryService;

    private ReplyKeyboardService replyKeyboardService;

    private MessageService messageService;

    private ReminderRequestService reminderRequestService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public CreateReminderCommand(LocalisationService localisationService, SavedQueryService savedQueryService,
                                 CurrReplyKeyboard replyKeyboardService, MessageService messageService,
                                 ReminderRequestService reminderRequestService, ReminderMessageSender reminderMessageSender) {
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME);
        this.localisationService = localisationService;
        this.savedQueryService = savedQueryService;
        this.replyKeyboardService = replyKeyboardService;
        this.messageService = messageService;
        this.reminderRequestService = reminderRequestService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean accept(Message message) {
        return message.hasText() || message.hasVideo();
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

        return true;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        Reminder reminder = reminderRequestService.createReminder(
                new ReminderRequestContext()
                        .setText(text)
                        .setVoice(message.hasVoice())
                        .setUser(message.getFrom())
                        .setMessageId(message.getMessageId()));
        reminderMessageSender.sendReminderCreated(reminder);
    }

    @Override
    public void processNonCommandEditedMessage(Message editedMessage, String text) {
        UpdateReminderResult updateReminderResult = reminderRequestService.updateReminder(editedMessage.getMessageId(), editedMessage.getFrom(), text);
        if (updateReminderResult == null) {
            return;
        }
        reminderMessageSender.sendReminderFullyUpdate(updateReminderResult);
    }

    @Override
    public ReplyKeyboardMarkup getKeyboard(long chatId) {
        List<String> queries = savedQueryService.getQueriesOnly((int) chatId);
        return replyKeyboardService.getSavedQueriesKeyboard(chatId, queries);
    }

    @Override
    public String getHistoryName() {
        return CommandNames.CREATE_REMINDER_COMMAND_NAME;
    }
}
