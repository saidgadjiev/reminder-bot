package ru.gadjini.reminder.bot.command.keyboard;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.FriendRequestExtractor;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

@Component
public class CreateReminderKeyboardCommand implements KeyboardBotCommand, NavigableBotCommand {

    private CommandStateService stateService;

    private String forFriendStart;

    private FriendRequestExtractor friendRequestExtractor;

    private ReminderRequestService reminderRequestService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private ReminderMessageSender reminderMessageSender;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    @Autowired
    public CreateReminderKeyboardCommand(CommandStateService stateService, LocalisationService localisationService,
                                         FriendRequestExtractor friendRequestExtractor,
                                         ReminderRequestService reminderRequestService, MessageService messageService,
                                         ReplyKeyboardService replyKeyboardService, CommandNavigator commandNavigator,
                                         ReminderMessageSender reminderMessageSender, FriendshipMessageBuilder friendshipMessageBuilder) {
        this.stateService = stateService;
        this.forFriendStart = localisationService.getMessage(MessagesProperties.FOR_FRIEND_REMINDER_START).toLowerCase();
        this.friendRequestExtractor = friendRequestExtractor;
        this.reminderRequestService = reminderRequestService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.commandNavigator = commandNavigator;
        this.reminderMessageSender = reminderMessageSender;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
    }

    @Override
    public boolean canHandle(String command) {
        return command != null && command.toLowerCase().startsWith(forFriendStart);
    }

    @Override
    public boolean accept(Message message) {
        return message.hasVoice() || message.hasText();
    }

    @Override
    public boolean processMessage(Message message, String text) {
        FriendRequestExtractor.ExtractReceiverResult extractReceiverResult = friendRequestExtractor.extractReceiver(message.getFrom().getId(), text, message.hasVoice());

        if (StringUtils.isBlank(extractReceiverResult.getText())) {
            TgUser receiver = extractReceiverResult.getReceiver();
            stateService.setState(message.getChatId(), receiver);
            messageService.sendMessage(
                    message.getChatId(),
                    friendshipMessageBuilder.getFriendDetailsWithFooterCode(receiver, MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT),
                    replyKeyboardService.goBackCommand()
            );
            return true;
        } else {
            TgUser receiver = extractReceiverResult.getReceiver();
            try {
                Reminder reminder = reminderRequestService.createReminder(new ReminderRequestContext()
                        .setText(extractReceiverResult.getText())
                        .setReceiverId(receiver.getUserId())
                        .setReceiverZone(receiver.getZone())
                        .setReceiverName(receiver.getName())
                        .setVoice(message.hasVoice())
                        .setUser(message.getFrom())
                        .setMessageId(message.getMessageId()));
                reminder.getCreator().setChatId(message.getChatId());
                reminderMessageSender.sendReminderCreated(reminder, null);

                return false;
            } catch (UserException ex) {
                stateService.setState(message.getChatId(), receiver);
                messageService.sendMessage(message.getChatId(), friendshipMessageBuilder.getFriendDetails(receiver, ex.getMessage()), replyKeyboardService.goBackCommand());

                return true;
            }
        }
    }

    @Override
    public void processEditedMessage(Message editedMessage, String text) {
        FriendRequestExtractor.ExtractReceiverResult extractReceiverResult = friendRequestExtractor.extractReceiver(editedMessage.getFrom().getId(), text, editedMessage.hasVoice());

        if (StringUtils.isNotBlank(extractReceiverResult.getText())) {
            UpdateReminderResult updateReminderResult = reminderRequestService.updateReminder(editedMessage.getMessageId(), extractReceiverResult.getText());
            updateReminderResult.getOldReminder().getCreator().setChatId(editedMessage.getChatId());
            reminderMessageSender.sendReminderFullyUpdate(updateReminderResult);
        }
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        TgUser receiver = stateService.getState(message.getChatId());
        Reminder reminder = reminderRequestService.createReminder(new ReminderRequestContext()
                .setText(text)
                .setReceiverId(receiver.getUserId())
                .setReceiverZone(receiver.getZone())
                .setVoice(message.hasVoice())
                .setUser(message.getFrom())
                .setReceiverName(receiver.getName())
                .setMessageId(message.getMessageId()));
        reminder.getCreator().setChatId(message.getChatId());

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderCreated(reminder, replyKeyboardMarkup);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

    @Override
    public String getHistoryName() {
        return CommandNames.CREATE_REMINDER_KEYBOARD_COMMAND_NAME;
    }
}
