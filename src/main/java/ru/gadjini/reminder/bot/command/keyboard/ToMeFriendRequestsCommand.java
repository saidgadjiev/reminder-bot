package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ToMeFriendRequestsCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private InlineKeyboardService inlineKeyboardService;

    private FriendshipService friendshipService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private MessageService messageService;

    private String name;

    @Autowired
    public ToMeFriendRequestsCommand(InlineKeyboardService inlineKeyboardService, LocalisationService localisationService,
                                     FriendshipService friendshipService, FriendshipMessageBuilder friendshipMessageBuilder, MessageService messageService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.friendshipService = friendshipService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.messageService = messageService;
        this.name = localisationService.getMessage(MessagesProperties.TO_ME_FRIEND_REQUESTS_COMMAND_NAME);
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<TgUser> friendRequests = friendshipService.getToMeFriendRequests(message.getFrom().getId());

        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(message.getChatId())
                        .text(friendshipMessageBuilder.getFriendsList(friendRequests, MessagesProperties.MESSAGE_FRIEND_REQUESTS_EMPTY, null))
                        .replyKeyboard(inlineKeyboardService.getFriendsListKeyboard(friendRequests.stream().map(TgUser::getUserId).collect(Collectors.toList()), CommandNames.GET_FRIEND_REQUEST_COMMAND_NAME))
        );
        return false;
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.TO_ME_FRIEND_REQUESTS_COMMAND_NAME;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<TgUser> friendRequests = friendshipService.getToMeFriendRequests(tgMessage.getUser().getId());

        messageService.editMessage(
                new EditMessageContext()
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(friendshipMessageBuilder.getFriendsList(friendRequests, MessagesProperties.MESSAGE_FRIEND_REQUESTS_EMPTY, null))
                        .replyKeyboard(inlineKeyboardService.getFriendsListKeyboard(friendRequests.stream().map(TgUser::getUserId).collect(Collectors.toList()), CommandNames.GET_FRIEND_REQUEST_COMMAND_NAME))
        );
    }
}
