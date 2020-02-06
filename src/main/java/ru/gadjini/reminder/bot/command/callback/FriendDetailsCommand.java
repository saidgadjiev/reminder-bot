package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class FriendDetailsCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private FriendshipService friendshipService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private LocalisationService localisationService;

    @Autowired
    public FriendDetailsCommand(FriendshipService friendshipService, MessageService messageService,
                                InlineKeyboardService inlineKeyboardService, FriendshipMessageBuilder friendshipMessageBuilder, LocalisationService localisationService) {
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.localisationService = localisationService;
    }

    @Override
    public String getName() {
        return CommandNames.FRIEND_DETAILS_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int friendUserId = requestParams.getInt(Arg.FRIEND_ID.getKey());
        TgUser friend = friendshipService.getFriend(callbackQuery.getFrom().getId(), friendUserId);
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(friendshipMessageBuilder.getFriendDetails(friend, localisationService.getCurrentLocale(callbackQuery.getFrom().getLanguageCode())))
                        .replyKeyboard(inlineKeyboardService.getFriendKeyboard(friendUserId, null))
        );
        return null;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        int friendUserId = requestParams.getInt(Arg.FRIEND_ID.getKey());
        TgUser friend = friendshipService.getFriend(tgMessage.getUser().getId(), friendUserId);

        if (friend == null) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(tgMessage.getChatId())
                            .messageId(tgMessage.getMessageId())
                            .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_FRIEND_NOT_FOUND, localisationService.getCurrentLocale(tgMessage.getUser().getLanguageCode())))
                            .replyKeyboard(inlineKeyboardService.getFriendKeyboard(friendUserId, null))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(tgMessage.getChatId())
                            .messageId(tgMessage.getMessageId())
                            .text(friendshipMessageBuilder.getFriendDetails(friend, localisationService.getCurrentLocale(tgMessage.getUser().getLanguageCode())))
                            .replyKeyboard(inlineKeyboardService.getFriendKeyboard(friendUserId, null))
            );
        }
    }
}
