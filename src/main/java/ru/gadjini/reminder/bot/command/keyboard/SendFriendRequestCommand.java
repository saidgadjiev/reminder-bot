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
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

@Component
public class SendFriendRequestCommand implements KeyboardBotCommand, NavigableBotCommand {

    private final LocalisationService localisationService;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private String name;

    @Autowired
    public SendFriendRequestCommand(LocalisationService localisationService,
                                    FriendshipService friendshipService,
                                    MessageService messageService,
                                    InlineKeyboardService inlineKeyboardService,
                                    ReplyKeyboardService replyKeyboardService,
                                    CommandNavigator commandNavigator) {
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.name = localisationService.getMessage(MessagesProperties.SEND_FRIEND_REQUEST_COMMAND_NAME);
        this.inlineKeyboardService = inlineKeyboardService;
        this.replyKeyboardService = replyKeyboardService;
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
    public boolean processMessage(Message message, String text) {
        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SEND_FRIEND_REQUEST_USERNAME))
                        .replyKeyboard(replyKeyboardService.goBackCommand())
        );
        return true;
    }

    @Override
    public boolean accept(Message message) {
        return message.hasContact() || message.hasText();
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CreateFriendRequestResult createFriendRequestResult;

        if (message.hasContact()) {
            Contact contact = message.getContact();

            createFriendRequestResult = friendshipService.createFriendRequest(TgMessage.from(message), contact.getUserID(), null);
        } else {
            String receiverName = removeUsernameStart(text);

            createFriendRequestResult = friendshipService.createFriendRequest(TgMessage.from(message), null, receiverName);
        }

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        switch (createFriendRequestResult.getState()) {
            case ALREADY_REQUESTED:
                messageService.sendMessage(new SendMessageContext().chatId(message.getChatId()).text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_REQUEST_ALREADY_SENT)));
                break;
            case ALREADY_REQUESTED_TO_ME:
                messageService.sendMessage(new SendMessageContext().chatId(message.getChatId()).text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_REQUEST_ALREADY_SENT_ME)));
                break;
            case ALREADY_FRIEND:
                messageService.sendMessage(new SendMessageContext().chatId(message.getChatId()).text(localisationService.getMessage(MessagesProperties.MESSAGE_ALREADY_FRIEND)));
                break;
            case NONE:
                Friendship friendship = createFriendRequestResult.getFriendship();

                messageService.sendMessage(
                        new SendMessageContext()
                                .chatId(message.getChatId())
                                .text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_REQUEST_SENT,
                                        new Object[]{UserUtils.userLink(friendship.getUserTwo())}))
                                .replyKeyboard(replyKeyboardMarkup)
                );
                messageService.sendMessage(
                        new SendMessageContext()
                                .chatId(friendship.getUserTwo().getChatId())
                                .text(localisationService.getMessage(MessagesProperties.MESSAGE_NEW_FRIEND_REQUEST, new Object[]{UserUtils.userLink(friendship.getUserOne())}))
                                .replyKeyboard(inlineKeyboardService.getFriendRequestKeyboard(friendship.getUserOne().getUserId()))
                );
                break;
        }
    }

    private String removeUsernameStart(String username) {
        return username.startsWith(TgUser.USERNAME_START) ? username.substring(1) : username;
    }
}
