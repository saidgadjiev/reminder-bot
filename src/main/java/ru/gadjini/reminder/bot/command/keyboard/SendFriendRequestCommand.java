package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.SendFriendRequestRequest;
import ru.gadjini.reminder.service.*;

public class SendFriendRequestCommand implements KeyboardBotCommand {

    private FriendshipService friendshipService;

    private MessageService messageService;

    private SendFriendRequestRequest sendFriendRequestRequest;

    private String name;

    private KeyboardService keyboardService;

    private CommandNavigator commandNavigator;

    public SendFriendRequestCommand(LocalisationService localisationService,
                                    FriendshipService friendshipService,
                                    MessageService messageService,
                                    KeyboardService keyboardService,
                                    CommandNavigator commandNavigator) {
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.name = localisationService.getMessage(MessagesProperties.SEND_FRIEND_REQUEST_COMMAND_NAME);
        this.keyboardService = keyboardService;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(AbsSender absSender, Message message) {
        sendFriendRequestRequest = new SendFriendRequestRequest();

        sendFriendRequestRequest.setInitiatorUserId(message.getFrom().getId());
        messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_SEND_FRIEND_REQUEST_USERNAME, keyboardService.goBackCommand());
    }

    @Override
    public void processNonCommandUpdate(AbsSender absSender, Message message) {
        String receiverName = removeUsernameStart(message.getText().trim());

        sendFriendRequestRequest.setReceiverUsername(receiverName);
        friendshipService.createFriendRequest(sendFriendRequestRequest);

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_SENT, new Object[]{message.getText().trim()}, replyKeyboardMarkup);
    }

    private String removeUsernameStart(String username) {
        return username.startsWith(TgUser.USERNAME_START) ? username.substring(1) : username;
    }
}
