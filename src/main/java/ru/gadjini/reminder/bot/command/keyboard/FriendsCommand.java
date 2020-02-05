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
import ru.gadjini.reminder.job.PriorityJob;
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
public class FriendsCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private InlineKeyboardService inlineKeyboardService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private String name;

    @Autowired
    public FriendsCommand(InlineKeyboardService inlineKeyboardService, FriendshipMessageBuilder friendshipMessageBuilder,
                          FriendshipService friendshipService, MessageService messageService, LocalisationService localisationService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.name = localisationService.getCurrentLocaleMessage(MessagesProperties.GET_FRIENDS_COMMAND_NAME);
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return this.name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<TgUser> friends = friendshipService.getFriends(message.getFrom().getId());

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(friendshipMessageBuilder.getFriendsList(friends, MessagesProperties.MESSAGE_FRIENDS_EMPTY, null))
                        .replyKeyboard(inlineKeyboardService.getFriendsListKeyboard(friends.stream().map(TgUser::getUserId).collect(Collectors.toList()), CommandNames.FRIEND_DETAILS_COMMAND_NAME))
        );

        return false;
    }

    @Override
    public String getName() {
        return CommandNames.GET_FRIENDS_COMMAND_HISTORY_NAME;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<TgUser> friends = friendshipService.getFriends(tgMessage.getUser().getId());

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(friendshipMessageBuilder.getFriendsList(friends, MessagesProperties.MESSAGE_FRIENDS_EMPTY, null))
                        .replyKeyboard(inlineKeyboardService.getFriendsListKeyboard(friends.stream().map(TgUser::getUserId).collect(Collectors.toList()), CommandNames.FRIEND_DETAILS_COMMAND_NAME))
        );
    }
}
