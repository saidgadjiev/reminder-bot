package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.bot.command.callback.tag.TagState;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Tag;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.AnswerCallbackContext;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.tag.ReminderTagService;
import ru.gadjini.reminder.service.tag.TagService;

import java.util.List;
import java.util.Locale;

@Component
public class TagCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private MessageService messageService;

    private LocalisationService localisationService;

    private TgUserService userService;

    private TagService tagService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderTagService reminderTagService;

    private CommandStateService commandStateService;

    @Autowired
    public TagCommand(MessageService messageService, LocalisationService localisationService,
                      TgUserService userService, TagService tagService,
                      InlineKeyboardService inlineKeyboardService, ReminderTagService reminderTagService,
                      CommandStateService commandStateService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.tagService = tagService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderTagService = reminderTagService;
        this.commandStateService = commandStateService;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public String getName() {
        return CommandNames.TAG_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        List<Tag> tags = tagService.tags(callbackQuery.getFrom().getId());
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());

        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());
        commandStateService.setState(callbackQuery.getFrom().getId(), new TagState(reminderId, callbackQuery.getMessage().getMessageId()));

        messageService.editMessage(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                .messageId(callbackQuery.getMessage().getMessageId())
                .chatId(callbackQuery.getFrom().getId())
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_TAG, locale))
                .replyKeyboard(inlineKeyboardService.getTagsKeyboard(reminderId, tags, locale))
        );

        return null;
    }

    @Override
    public void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {
        int tagId = requestParams.getInt(Arg.TAG_ID.getKey());
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());

        reminderTagService.tag(reminderId, tagId);
        messageService.sendAnswerCallbackQuery(
                new AnswerCallbackContext()
                .queryId(callbackQuery.getId())
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_TAG_ADDED_ANSWER,
                        userService.getLocale(callbackQuery.getFrom().getId())))
        );
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        tagService.create(message.getFrom().getId(), text);
        List<Tag> tags = tagService.tags(message.getFrom().getId());
        TagState tagState = commandStateService.getState(message.getFrom().getId(), true);
        messageService.editReplyKeyboard(message.getFrom().getId(), tagState.getMessageId(),
                inlineKeyboardService.getTagsKeyboard(tagState.getReminderId(), tags, userService.getLocale(message.getFrom().getId())));
    }

    @Override
    public void leave(long chatId) {
        commandStateService.deleteState(chatId);
    }
}
