package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

import java.util.Locale;

@Component
public class ChangeReminderTimeCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderRequestService reminderService;

    private CallbackCommandNavigator commandNavigator;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    private TgUserService userService;

    @Autowired
    public ChangeReminderTimeCommand(CommandStateService stateService,
                                     ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderRequestService reminderService,
                                     InlineKeyboardService inlineKeyboardService,
                                     LocalisationService localisationService, TgUserService userService) {
        this.stateService = stateService;
        this.localisationService = localisationService;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.userService = userService;
    }

    @Autowired
    public void setCommandNavigator(CallbackCommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.EDIT_REMINDER_TIME_COMMAND_NAME;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams, null));

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME, locale))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.EDIT_REMINDER_COMMAND_NAME, requestParams, locale))
        );

        return MessagesProperties.MESSAGE_REMINDER_TIME_ANSWER;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest request = stateService.getState(message.getChatId(), true);
        UpdateReminderResult updateReminderResult = reminderService.changeReminderTime(request.getRequestParams().getInt(Arg.REMINDER_ID.getKey()), text);

        if (updateReminderResult == null) {
            reminderMessageSender.sendReminderNotFound(message.getChatId(), message.getMessageId(), userService.getLocale(message.getFrom().getId()));
            return;
        }

        commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderTimeChanged(request.getMessageId(), updateReminderResult);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }
}
