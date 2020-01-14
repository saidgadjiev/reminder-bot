package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageBuilder;

@Component
public class CreatorReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderService reminderService;

    private MessageService messageService;

    private ReminderMessageBuilder messageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    @Autowired
    public CreatorReminderCommand(ReminderService reminderService, MessageService messageService,
                                  ReminderMessageBuilder messageBuilder, InlineKeyboardService inlineKeyboardService, LocalisationService localisationService) {
        this.reminderService = reminderService;
        this.messageService = messageService;
        this.messageBuilder = messageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
    }

    @Override
    public String getName() {
        return CommandNames.CREATOR_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        return null;
    }

    @Override
    public String getHistoryName() {
        return getName();
    }


    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(messageBuilder.getReminderMessage(reminder, reminder.getCreatorId()))
                        .replyKeyboard(inlineKeyboardService.getCreatorReminderKeyboard(reminder))
        );
        if (replyKeyboard != null) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(tgMessage.getChatId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_HOW_HELP))
                            .replyKeyboard(replyKeyboard)
            );
        }
    }
}
