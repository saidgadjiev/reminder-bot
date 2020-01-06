package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

@Component
public class DeleteFriendCommand implements CallbackBotCommand {

    private MessageService messageService;

    private FriendshipService friendshipService;

    private InlineKeyboardService inlineKeyboardService;

    private String name;

    @Autowired
    public DeleteFriendCommand(MessageService messageService, FriendshipService friendshipService, InlineKeyboardService inlineKeyboardService) {
        this.messageService = messageService;
        this.friendshipService = friendshipService;
        this.inlineKeyboardService = inlineKeyboardService;
        name = CommandNames.DELETE_FRIEND_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        FriendshipService.DeleteFriendResult deleteFriendResult = friendshipService.deleteFriend(TgMessage.from(callbackQuery), requestParams.getInt(Arg.FRIEND_ID.getKey()));

        messageService.editMessageByMessageCode(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                MessagesProperties.MESSAGE_FRIEND_DELETED,
                inlineKeyboardService.goBackCallbackButton(CommandNames.GET_FRIENDS_COMMAND_HISTORY_NAME)
        );
        if (deleteFriendResult.getReminders().size() > 0) {
            sendRemindersDeleted(callbackQuery.getMessage().getChatId(), callbackQuery.getFrom().getId(), deleteFriendResult);
        }

        return MessagesProperties.MESSAGE_FRIEND_DELETED_ANSWER;
    }

    private void sendRemindersDeleted(long chatId, int userId, FriendshipService.DeleteFriendResult deleteFriendResult) {
        sendRemindersDeletedToFormerFriend(userId, deleteFriendResult);
        deleteToMeReminders(chatId, userId, deleteFriendResult);
    }

    private void deleteToMeReminders(long chatId, int userId, FriendshipService.DeleteFriendResult deleteFriendResult) {
        deleteFriendResult.getReminders().stream()
                .filter(reminder -> reminder.getReceiverId() == userId)
                .forEach(reminder -> messageService.deleteMessage(chatId, reminder.getRemindMessage().getMessageId()));
    }

    private void sendRemindersDeletedToFormerFriend(int userId, FriendshipService.DeleteFriendResult deleteFriendResult) {
        Friendship friendship = deleteFriendResult.getFriendship();
        TgUser friend = friendship.getFriend(userId);

        messageService.sendMessageByCode(friend.getChatId(), MessagesProperties.MESSAGE_FRIENDSHIP_INTERRUPTED, new Object[]{
                UserUtils.userLink(friendship.getUser(userId))
        });

        deleteFriendResult.getReminders().stream()
                .filter(reminder -> reminder.getCreatorId() == userId)
                .forEach(reminder -> messageService.deleteMessage(friend.getChatId(), reminder.getRemindMessage().getMessageId()));
    }
}
