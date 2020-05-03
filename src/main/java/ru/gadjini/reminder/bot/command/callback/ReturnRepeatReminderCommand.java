package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService;

import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.ActionResult;
import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.ReminderActionResult;

@Component
public class ReturnRepeatReminderCommand implements CallbackBotCommand {

    private RepeatReminderBusinessService repeatReminderService;

    private ReminderMessageSender messageSender;

    @Autowired
    public ReturnRepeatReminderCommand(RepeatReminderBusinessService repeatReminderService, ReminderMessageSender messageSender) {
        this.repeatReminderService = repeatReminderService;
        this.messageSender = messageSender;
    }

    @Override
    public String getName() {
        return CommandNames.RETURN_REPEAT_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        ReminderActionResult returnReminderResult = repeatReminderService.returnReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        if (returnReminderResult.getActionResult() == ActionResult.NOT_RETURNED) {
            messageSender.sendRepeatReminderCantBeReturnedFromList(
                    callbackQuery.getMessage().getChatId(),
                    callbackQuery.getMessage().getMessageId(),
                    callbackQuery.getMessage().getReplyMarkup(),
                    returnReminderResult.getReminder().getReceiver().getLocale()
            );

            return MessagesProperties.MESSAGE_REMINDER_CANT_BE_RETURNED_ANSWER;
        } else {
            messageSender.sendRepeatReminderReturnedFromList(
                    callbackQuery.getMessage().getChatId(),
                    callbackQuery.getMessage().getMessageId(),
                    callbackQuery.getMessage().getReplyMarkup(),
                    returnReminderResult.getReminder()
            );

            return MessagesProperties.MESSAGE_REMINDER_RETURNED_ANSWER;
        }
    }
}
