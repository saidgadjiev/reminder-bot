package ru.gadjini.reminder.bot.command.keyboard;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.FriendRequestExtractor;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class CreateForFriendReminderKeyboardCommand implements KeyboardBotCommand, NavigableBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateForFriendReminderKeyboardCommand.class);

    private CommandStateService stateService;

    private Set<String> forFriendStart = new HashSet<>();

    private FriendRequestExtractor friendRequestExtractor;

    private ReminderRequestService reminderRequestService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private ReminderMessageSender reminderMessageSender;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private TgUserService userService;

    @Autowired
    public CreateForFriendReminderKeyboardCommand(CommandStateService stateService, LocalisationService localisationService,
                                                  FriendRequestExtractor friendRequestExtractor,
                                                  ReminderRequestService reminderRequestService, MessageService messageService,
                                                  CurrReplyKeyboard replyKeyboardService, CommandNavigator commandNavigator,
                                                  ReminderMessageSender reminderMessageSender, FriendshipMessageBuilder friendshipMessageBuilder, TgUserService userService) {
        this.stateService = stateService;
        this.friendRequestExtractor = friendRequestExtractor;
        this.reminderRequestService = reminderRequestService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.commandNavigator = commandNavigator;
        this.reminderMessageSender = reminderMessageSender;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.forFriendStart.add(localisationService.getMessage(MessagesProperties.FOR_FRIEND_REMINDER_START, locale).toLowerCase());
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        if (!commandNavigator.isCurrentCommandThat(chatId, CommandNames.START_COMMAND_NAME)) {
            return false;
        }

        return command != null && forFriendStart.stream().anyMatch(command::startsWith);
    }

    @Override
    public boolean accept(Message message) {
        return message.hasVoice() || message.hasText();
    }

    @Override
    public boolean isTextCommand() {
        return true;
    }

    @Override
    public boolean processMessage(Message message, String text) {
        Locale locale = userService.getLocale(message.getFrom().getId());
        FriendRequestExtractor.ExtractReceiverResult extractReceiverResult = friendRequestExtractor.extractReceiver(message.getFrom().getId(), text, message.hasVoice(), locale);

        if (StringUtils.isBlank(extractReceiverResult.getText())) {
            TgUser receiver = extractReceiverResult.getReceiver();
            stateService.setState(message.getChatId(), receiver);
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(message.getChatId())
                            .text(friendshipMessageBuilder.getFriendDetailsWithFooterCode(receiver, MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT, locale))
                            .replyKeyboard(replyKeyboardService.goBackCommand(message.getChatId(), locale))
            );
            return true;
        } else {
            TgUser receiver = extractReceiverResult.getReceiver();
            try {
                Reminder reminder = reminderRequestService.createReminder(new ReminderRequestContext()
                        .setText(extractReceiverResult.getText())
                        .setReceiver(receiver)
                        .setVoice(message.hasVoice())
                        .setUser(message.getFrom())
                        .setMessageId(message.getMessageId()));
                reminderMessageSender.sendReminderCreated(reminder);

                return false;
            } catch (UserException ex) {
                LOGGER.error(ex.getMessage());
                stateService.setState(message.getChatId(), receiver);
                messageService.sendMessageAsync(
                        new SendMessageContext(PriorityJob.Priority.MEDIUM)
                                .chatId(message.getChatId())
                                .text(friendshipMessageBuilder.getFriendDetails(receiver, ex.getMessage(), locale))
                                .replyKeyboard(replyKeyboardService.goBackCommand(message.getChatId(), locale))
                );

                return true;
            }
        }
    }

    @Override
    public void processEditedMessage(Message editedMessage, String text) {
        Locale locale = userService.getLocale(editedMessage.getFrom().getId());
        FriendRequestExtractor.ExtractReceiverResult extractReceiverResult = friendRequestExtractor.extractReceiver(
                editedMessage.getFrom().getId(), text, editedMessage.hasVoice(), locale);

        if (StringUtils.isNotBlank(extractReceiverResult.getText())) {
            UpdateReminderResult updateReminderResult = reminderRequestService.updateReminder(editedMessage.getMessageId(), editedMessage.getFrom(), extractReceiverResult.getText());
            reminderMessageSender.sendReminderFullyUpdate(updateReminderResult);
        }
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        TgUser receiver = stateService.getState(message.getChatId(), true);
        Reminder reminder = reminderRequestService.createReminder(new ReminderRequestContext()
                .setText(text)
                .setReceiver(receiver)
                .setVoice(message.hasVoice())
                .setUser(message.getFrom())
                .setMessageId(message.getMessageId()));
        reminderMessageSender.sendReminderCreated(reminder);
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
