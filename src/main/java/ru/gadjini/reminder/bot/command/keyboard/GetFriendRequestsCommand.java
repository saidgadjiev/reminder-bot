package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

import java.util.List;

@Component
public class GetFriendRequestsCommand implements KeyboardBotCommand {

    private KeyboardService keyboardService;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private String name;

    @Autowired
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
            messageService.sendMessage(chatId, UserUtils.userLink(friend), keyboardService.getFriendRequestKeyboard(friend.getUserId()));
        }
    }
}
