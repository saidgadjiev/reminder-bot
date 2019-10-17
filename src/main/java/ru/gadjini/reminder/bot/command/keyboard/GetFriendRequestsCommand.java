package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.KeyboardService;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.MessageService;

import java.util.List;

public class GetFriendRequestsCommand implements KeyboardBotCommand {

    private KeyboardService keyboardService;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private String name;

    public GetFriendRequestsCommand(KeyboardService keyboardService, LocalisationService localisationService, FriendshipService friendshipService, MessageService messageService) {
        this.keyboardService = keyboardService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.name = localisationService.getMessage(MessagesProperties.GET_FRIEND_REQUESTS_COMMAND_NAME);
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(AbsSender absSender, Message message) {
        List<TgUser> friendRequests = friendshipService.getFriendRequests(message.getFrom().getUserName());

        sendFriendRequests(message.getChatId(), friendRequests);
    }

    private void sendFriendRequests(long chatId, List<TgUser> friendRequests) {
        for (TgUser friend : friendRequests) {
            StringBuilder friendRequest = new StringBuilder();

            friendRequest.append("<b>").append(friend.getFio()).append("</b>\n");
            friendRequest.append(TgUser.USERNAME_START).append(friend.getUsername());

            messageService.sendMessage(chatId, friendRequest.toString(), keyboardService.getFriendRequestKeyboard(friend.getId()));
        }
    }
}
