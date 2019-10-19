package ru.gadjini.reminder.bot.command.keyboard;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
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
    public void processMessage(Message message) {
        List<TgUser> friendRequests = friendshipService.getFriendRequests();

        sendFriendRequests(message.getChatId(), friendRequests);
    }

    private void sendFriendRequests(long chatId, List<TgUser> friendRequests) {
        for (TgUser friend : friendRequests) {
            StringBuilder friendRequest = new StringBuilder();

            if (StringUtils.isNotBlank(friend.getLastName())) {
                friendRequest.append("<b>").append(friend.getFio()).append("</b>\n");
                friendRequest.append(TgUser.USERNAME_START).append(friend.getUsername());
            } else {
                friendRequest.append("<b>").append(friend.getFio()).append("</b> ");
                friendRequest.append(TgUser.USERNAME_START).append(friend.getUsername());
            }

            messageService.sendMessage(chatId, friendRequest.toString(), keyboardService.getFriendRequestKeyboard(friend.getUserId()));
        }
    }
}
