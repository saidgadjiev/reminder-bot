package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

import java.util.Locale;

@Component
public class DeleteFriendCommand implements CallbackBotCommand {

    private MessageService messageService;

    private FriendshipService friendshipService;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    private TgUserService userService;

    private String name;

    @Autowired
    public DeleteFriendCommand(MessageService messageService, FriendshipService friendshipService, InlineKeyboardService inlineKeyboardService, LocalisationService localisationService, TgUserService userService) {
        this.messageService = messageService;
        this.friendshipService = friendshipService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
        this.userService = userService;
        name = CommandNames.DELETE_FRIEND_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        FriendshipService.DeleteFriendResult deleteFriendResult = friendshipService.deleteFriend(TgMessage.from(callbackQuery), requestParams.getInt(Arg.FRIEND_ID.getKey()));

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_DELETED, locale))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_FRIENDS_COMMAND_HISTORY_NAME, locale))
        );
        if (deleteFriendResult.getReminders().size() > 0) {
            sendRemindersDeleted(callbackQuery.getMessage().getChatId(), callbackQuery.getFrom().getId(), deleteFriendResult);
        }

        return MessagesProperties.MESSAGE_FRIEND_DELETED_ANSWER;
    }

    private void sendRemindersDeleted(long chatId, long userId, FriendshipService.DeleteFriendResult deleteFriendResult) {
        sendRemindersDeletedToFormerFriend(userId, deleteFriendResult);
        deleteToMeReminders(chatId, userId, deleteFriendResult);
    }

    private void deleteToMeReminders(long chatId, long userId, FriendshipService.DeleteFriendResult deleteFriendResult) {
        deleteFriendResult.getReminders().stream()
                .filter(reminder -> reminder.getReceiverId() == userId)
                .forEach(reminder -> messageService.deleteMessage(chatId, reminder.getReceiverMessageId()));
    }

    private void sendRemindersDeletedToFormerFriend(long userId, FriendshipService.DeleteFriendResult deleteFriendResult) {
        Friendship friendship = deleteFriendResult.getFriendship();
        TgUser friend = friendship.getFriend(userId);

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(friend.getUserId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIENDSHIP_INTERRUPTED, new Object[]{
                                UserUtils.userLink(friendship.getUser(userId))
                        }, friend.getLocale()))
        );

        deleteFriendResult.getReminders().stream()
                .filter(reminder -> reminder.getCreatorId() == userId)
                .forEach(reminder -> messageService.deleteMessage(friend.getUserId(), reminder.getReceiverMessageId()));
    }
}
