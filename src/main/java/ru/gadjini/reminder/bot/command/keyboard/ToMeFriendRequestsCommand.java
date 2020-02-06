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
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ToMeFriendRequestsCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private InlineKeyboardService inlineKeyboardService;

    private FriendshipService friendshipService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private MessageService messageService;

    private TgUserService userService;

    private Set<String> names = new HashSet<>();

    @Autowired
    public ToMeFriendRequestsCommand(InlineKeyboardService inlineKeyboardService, LocalisationService localisationService,
                                     FriendshipService friendshipService, FriendshipMessageBuilder friendshipMessageBuilder, MessageService messageService, TgUserService userService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.friendshipService = friendshipService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.messageService = messageService;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.TO_ME_FRIEND_REQUESTS_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<TgUser> friendRequests = friendshipService.getToMeFriendRequests(message.getFrom().getId());

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(friendshipMessageBuilder.getFriendsList(friendRequests, MessagesProperties.MESSAGE_FRIEND_REQUESTS_EMPTY, null, userService.getLocale(message.getFrom().getId())))
                        .replyKeyboard(inlineKeyboardService.getFriendsListKeyboard(friendRequests.stream().map(TgUser::getUserId).collect(Collectors.toList()), CommandNames.GET_FRIEND_REQUEST_COMMAND_NAME))
        );
        return false;
    }

    @Override
    public String getName() {
        return CommandNames.TO_ME_FRIEND_REQUESTS_COMMAND_NAME;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<TgUser> friendRequests = friendshipService.getToMeFriendRequests(tgMessage.getUser().getId());

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(friendshipMessageBuilder.getFriendsList(friendRequests, MessagesProperties.MESSAGE_FRIEND_REQUESTS_EMPTY, null, userService.getLocale(tgMessage.getUser().getId())))
                        .replyKeyboard(inlineKeyboardService.getFriendsListKeyboard(friendRequests.stream().map(TgUser::getUserId).collect(Collectors.toList()), CommandNames.GET_FRIEND_REQUEST_COMMAND_NAME))
        );
    }
}
