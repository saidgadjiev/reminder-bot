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
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class ReceiverReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderMessageBuilder messageBuilder;

    private ReminderService reminderService;

    private LocalisationService localisationService;

    @Autowired
    public ReceiverReminderCommand(ReminderMessageSender reminderMessageSender, MessageService messageService,
                                   InlineKeyboardService inlineKeyboardService, ReminderMessageBuilder messageBuilder,
                                   ReminderService reminderService, LocalisationService localisationService) {
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageBuilder = messageBuilder;
        this.reminderService = reminderService;
        this.localisationService = localisationService;
    }

    @Override
    public String getName() {
        return CommandNames.RECEIVER_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderMessageSender.sendReminderEdit(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), requestParams.getInt(Arg.REMINDER_ID.getKey()));
        return null;
    }

    @Override
    public String getHistoryName() {
        return getName();
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                new EditMessageContext()
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(messageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(requestParams.getInt(Arg.REMINDER_ID.getKey()), reminder.isRepeatable(), reminder.getRemindAt().hasTime()))
        );
        if (replyKeyboard != null) {
            messageService.sendMessage(
                    new SendMessageContext()
                            .chatId(tgMessage.getChatId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_HOW_HELP))
                            .replyKeyboard(replyKeyboard)
            );
        }
    }
}
