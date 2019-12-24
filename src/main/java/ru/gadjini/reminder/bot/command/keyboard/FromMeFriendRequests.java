package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FromMeFriendRequests implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private String name;

    private FriendshipService friendshipService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    private MessageService messageService;

    @Autowired
    public FromMeFriendRequests(LocalisationService localisationService, FriendshipService friendshipService,
                                FriendshipMessageBuilder friendshipMessageBuilder, InlineKeyboardService inlineKeyboardService, MessageService messageService) {
        this.name = localisationService.getMessage(MessagesProperties.FROM_ME_FRIEND_REQUESTS_COMMAND_NAME);
        this.friendshipService = friendshipService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageService = messageService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        List<TgUser> requests = friendshipService.getFromMeFriendRequests();
        messageService.sendMessage(
                message.getChatId(),
                friendshipMessageBuilder.getFriendsList(requests, MessagesProperties.MESSAGE_FROM_ME_FRIEND_REQUESTS_EMPTY, MessagesProperties.MESSAGE_CHOOSE_FRIEND_REQUEST_CANCEL),
                inlineKeyboardService.getFriendsListKeyboard(requests.stream().map(TgUser::getUserId).collect(Collectors.toList()), MessagesProperties.CANCEL_FRIEND_REQUEST_COMMAND_NAME)
        );
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.FROM_ME_FRIEND_REQUESTS_HISTORY_NAME;
    }
}
