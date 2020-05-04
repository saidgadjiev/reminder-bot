package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CreateFriendRequestResult;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class SendFriendRequestCommand implements KeyboardBotCommand, NavigableBotCommand {

    private final LocalisationService localisationService;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private TgUserService userService;

    private Set<String> names = new HashSet<>();

    @Autowired
    public SendFriendRequestCommand(LocalisationService localisationService,
                                    FriendshipService friendshipService,
                                    MessageService messageService,
                                    InlineKeyboardService inlineKeyboardService,
                                    CurrReplyKeyboard replyKeyboardService,
                                    TgUserService userService) {
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.replyKeyboardService = replyKeyboardService;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.SEND_FRIEND_REQUEST_COMMAND_NAME, locale));
        }
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.SEND_FRIEND_REQUEST_COMMAND_NAME;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        Locale locale = userService.getLocale(message.getFrom().getId());
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SEND_FRIEND_REQUEST_USERNAME, locale))
                        .replyKeyboard(replyKeyboardService.goBackCommand(message.getChatId(), locale))
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

        Locale uoLocale = userService.getLocale(message.getFrom().getId());
        if (message.hasContact()) {
            Contact contact = message.getContact();

            if (contact.getUserID() == null) {
                messageService.sendMessageAsync(
                        new SendMessageContext(PriorityJob.Priority.HIGH)
                                .chatId(message.getChatId())
                                .text(localisationService.getMessage(MessagesProperties.MESSAGE_CONTACT_ID_EMPTY, uoLocale))
                );

                return;
            }
            createFriendRequestResult = friendshipService.createFriendRequest(TgMessage.from(message), contact.getUserID(), null);
        } else {
            String receiverName = removeUsernameStart(text);

            createFriendRequestResult = friendshipService.createFriendRequest(TgMessage.from(message), null, receiverName);
        }

        switch (createFriendRequestResult.getState()) {
            case ALREADY_REQUESTED:
                messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH).chatId(message.getChatId()).text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_REQUEST_ALREADY_SENT, uoLocale)));
                break;
            case ALREADY_REQUESTED_TO_ME:
                messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH).chatId(message.getChatId()).text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_REQUEST_ALREADY_SENT_ME, uoLocale)));
                break;
            case ALREADY_FRIEND:
                messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH).chatId(message.getChatId()).text(localisationService.getMessage(MessagesProperties.MESSAGE_ALREADY_FRIEND, uoLocale)));
                break;
            case NONE:
                Friendship friendship = createFriendRequestResult.getFriendship();

                ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());
                messageService.sendMessageAsync(
                        new SendMessageContext(PriorityJob.Priority.HIGH)
                                .chatId(message.getChatId())
                                .text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_REQUEST_SENT,
                                        new Object[]{UserUtils.userLink(friendship.getUserTwo())}, uoLocale))
                                .replyKeyboard(replyKeyboardMarkup)
                );
                messageService.sendMessageAsync(
                        new SendMessageContext(PriorityJob.Priority.MEDIUM)
                                .chatId(friendship.getUserTwoId())
                                .text(localisationService.getMessage(MessagesProperties.MESSAGE_NEW_FRIEND_REQUEST, new Object[]{UserUtils.userLink(friendship.getUserOne())}, friendship.getUserTwo().getLocale()))
                                .replyKeyboard(inlineKeyboardService.getFriendRequestKeyboard(friendship.getUserOne().getUserId(), friendship.getUserTwo().getLocale()))
                );
                break;
        }
    }

    private String removeUsernameStart(String username) {
        return username.startsWith(TgUser.USERNAME_START) ? username.substring(1) : username;
    }
}
