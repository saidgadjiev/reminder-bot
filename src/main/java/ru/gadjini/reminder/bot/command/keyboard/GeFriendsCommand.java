package ru.gadjini.reminder.bot.command.keyboard;

import org.apache.commons.lang3.StringUtils;
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

public class GeFriendsCommand implements KeyboardBotCommand {

    private KeyboardService keyboardService;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private String name;

    public GeFriendsCommand(KeyboardService keyboardService, FriendshipService friendshipService, MessageService messageService, LocalisationService localisationService) {
        this.keyboardService = keyboardService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.name = localisationService.getMessage(MessagesProperties.GET_FRIENDS_COMMAND_NAME);
    }

    @Override
    public boolean canHandle(String command) {
        return this.name.equals(command);
    }

    @Override
    public void processMessage(AbsSender absSender, Message message) {
        List<TgUser> friends = friendshipService.getFriends(message.getFrom().getUserName());

        sendFriends(message.getChatId(), friends);
    }

    private void sendFriends(long chatId, List<TgUser> friends) {
        for (TgUser tgUser : friends) {
            StringBuilder friendMsg = new StringBuilder();

            if (StringUtils.isNotBlank(tgUser.getLastName())) {
                friendMsg.append("<b>").append(tgUser.getFio()).append("</b>\n");
                friendMsg.append("@").append(tgUser.getUsername());
            } else {
                friendMsg.append("<b>").append(tgUser.getFio()).append("</b> ");
                friendMsg.append("@").append(tgUser.getUsername());
            }

            messageService.sendMessage(chatId, friendMsg.toString(), keyboardService.getFriendKeyboard(tgUser.getId()));
        }
    }
}
