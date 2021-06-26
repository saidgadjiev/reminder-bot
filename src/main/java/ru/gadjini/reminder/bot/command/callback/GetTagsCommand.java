package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Tag;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.tag.ReminderTagService;

import java.util.List;
import java.util.Locale;

@Component
public class GetTagsCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderTagService reminderTagService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private TgUserService userService;

    private LocalisationService localisationService;

    @Autowired
    public GetTagsCommand(ReminderTagService reminderTagService,
                          MessageService messageService, InlineKeyboardService inlineKeyboardService,
                          TgUserService userService, LocalisationService localisationService) {
        this.reminderTagService = reminderTagService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.userService = userService;
        this.localisationService = localisationService;
    }

    @Override
    public String getName() {
        return CommandNames.TAGS_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        List<Tag> tags = reminderTagService.getTags(callbackQuery.getFrom().getId());

        messageService.editReplyKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(),
                inlineKeyboardService.reminderTagsKeyboard(tags, userService.getLocale(callbackQuery.getFrom().getId())));

        return null;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<Tag> tags = reminderTagService.getTags(tgMessage.getUser().getId());

        Locale locale = userService.getLocale(tgMessage.getUser().getId());
        messageService.editMessage(new EditMessageContext(PriorityJob.Priority.HIGH)
                .chatId(tgMessage.getUser().getId())
                .messageId(tgMessage.getMessageId())
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_LET_SEE_ON_REMINDERS, locale))
                .replyKeyboard(inlineKeyboardService.reminderTagsKeyboard(tags, locale)));
    }
}
