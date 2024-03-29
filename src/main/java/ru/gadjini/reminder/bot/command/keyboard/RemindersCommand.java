package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class RemindersCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private Set<String> names = new HashSet<>();

    private final LocalisationService localisationService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private TgUserService userService;

    @Autowired
    public RemindersCommand(LocalisationService localisationService, MessageService messageService, InlineKeyboardService inlineKeyboardService, TgUserService userService) {
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.GET_REMINDERS_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        Locale locale = userService.getLocale(message.getFrom().getId());
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_LET_SEE_ON_REMINDERS, locale))
                        .replyKeyboard(inlineKeyboardService.getRemindersMenu(locale))
        );

        return false;
    }

    @Override
    public String getName() {
        return CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_LET_SEE_ON_REMINDERS, userService.getLocale(tgMessage.getUser().getId())))
                        .replyKeyboard(inlineKeyboardService.getRemindersMenu(null))
        );
    }
}
