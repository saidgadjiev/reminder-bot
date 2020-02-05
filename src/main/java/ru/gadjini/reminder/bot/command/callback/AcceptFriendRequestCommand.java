package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

@Component
public class AcceptFriendRequestCommand implements CallbackBotCommand {

    private String name;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private LocalisationService localisationService;

    @Autowired
    public AcceptFriendRequestCommand(FriendshipService friendshipService, MessageService messageService,
                                      InlineKeyboardService inlineKeyboardService,
                                      FriendshipMessageBuilder friendshipMessageBuilder, LocalisationService localisationService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.localisationService = localisationService;
        this.name = CommandNames.ACCEPT_FRIEND_REQUEST_COMMAND_NAME;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Friendship friendship = friendshipService.acceptFriendRequest(callbackQuery.getFrom(), requestParams.getInt(Arg.FRIEND_ID.getKey()));

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(friendship.getUserOneId())
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_FRIEND_REQUEST_ACCEPTED_INITIATOR, new Object[]{
                                UserUtils.userLink(friendship.getUserTwo())
                        }))
        );

        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(friendshipMessageBuilder.getFriendDetails(friendship.getUserOne()))
                        .replyKeyboard(inlineKeyboardService.getFriendKeyboard(friendship.getUserOneId()))
        );

        return MessagesProperties.MESSAGE_FRIEND_REQUEST_ACCEPTED_ANSWER;
    }
}
