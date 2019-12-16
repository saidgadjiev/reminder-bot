package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.CreateFriendRequestResult;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

@Component
public class SendFriendRequestCommand implements KeyboardBotCommand, NavigableBotCommand {

    private FriendshipService friendshipService;

    private MessageService messageService;

    private String name;

    private KeyboardService keyboardService;

    private CommandNavigator commandNavigator;

    @Autowired
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
    public String getHistoryName() {
        return name;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_SEND_FRIEND_REQUEST_USERNAME, keyboardService.goBackCommand());
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        CreateFriendRequestResult createFriendRequestResult;

        if (message.hasContact()) {
            Contact contact = message.getContact();

            createFriendRequestResult = friendshipService.createFriendRequest(contact.getUserID(), null);
        } else {
            String receiverName = removeUsernameStart(message.getText().trim());

            createFriendRequestResult = friendshipService.createFriendRequest(null, receiverName);
        }

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        switch (createFriendRequestResult.getState()) {
            case ALREADY_REQUESTED:
                messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_ALREADY_SENT);
                break;
            case ALREADY_REQUESTED_TO_ME:
                messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_ALREADY_SENT_ME);
                break;
            case ALREADY_FRIEND:
                messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_ALREADY_FRIEND);
                break;
            case NONE:
                Friendship friendship = createFriendRequestResult.getFriendship();

                messageService.sendMessageByCode(
                        message.getChatId(),
                        MessagesProperties.MESSAGE_FRIEND_REQUEST_SENT,
                        new Object[]{UserUtils.userLink(friendship.getUserTwo())},
                        replyKeyboardMarkup
                );
                messageService.sendMessageByCode(
                        friendship.getUserTwo().getChatId(),
                        MessagesProperties.MESSAGE_NEW_FRIEND_REQUEST,
                        new Object[]{UserUtils.userLink(friendship.getUserOne())},
                        keyboardService.getFriendRequestKeyboard(friendship.getUserOne().getUserId())
                );
                break;
        }
    }

    private String removeUsernameStart(String username) {
        return username.startsWith(TgUser.USERNAME_START) ? username.substring(1) : username;
    }
}
