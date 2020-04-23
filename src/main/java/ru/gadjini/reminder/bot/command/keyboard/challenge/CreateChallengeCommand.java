package ru.gadjini.reminder.bot.command.keyboard.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.bot.command.state.ReminderRequestData;
import ru.gadjini.reminder.bot.command.state.TimeData;
import ru.gadjini.reminder.bot.command.state.UserData;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CreateChallengeRequest;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.challenge.ChallengeMessageBuilder;
import ru.gadjini.reminder.service.challenge.ChallengeService;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.reminder.TimeRequestService;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestExtractor;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CreateChallengeCommand implements KeyboardBotCommand, CallbackBotCommand, NavigableCallbackBotCommand {

    private Set<String> names = new HashSet<>();

    private final LocalisationService localisationService;

    private MessageService messageService;

    private TgUserService userService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderRequestExtractor requestExtractor;

    private CommandStateService commandStateService;

    private TimeRequestService timeRequestService;

    private FriendshipService friendshipService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private ChallengeService challengeService;

    private ChallengeMessageBuilder challengeMessageBuilder;

    private CallbackCommandNavigator commandNavigator;

    @Autowired
    public CreateChallengeCommand(LocalisationService localisationService, MessageService messageService,
                                  TgUserService userService, InlineKeyboardService inlineKeyboardService, @Qualifier("chain") ReminderRequestExtractor requestExtractor,
                                  CommandStateService commandStateService, TimeRequestService timeRequestService,
                                  FriendshipService friendshipService, FriendshipMessageBuilder friendshipMessageBuilder,
                                  ChallengeService challengeService, ChallengeMessageBuilder challengeMessageBuilder) {
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.userService = userService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.requestExtractor = requestExtractor;
        this.commandStateService = commandStateService;
        this.timeRequestService = timeRequestService;
        this.friendshipService = friendshipService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.challengeService = challengeService;
        this.challengeMessageBuilder = challengeMessageBuilder;
        for (Locale locale : localisationService.getSupportedLocales()) {
            names.add(localisationService.getMessage(MessagesProperties.CREATE_CHALLENGE_COMMAND_NAME, locale));
        }
    }

    @Autowired
    public void setCommandNavigator(CallbackCommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public boolean processMessage(Message message, String text) {
        ChallengeState state = new ChallengeState();
        Locale locale = userService.getLocale(message.getFrom().getId());
        state.setUserLanguage(locale.getLanguage());

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CREATE_CHALLENGE, locale))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.START_COMMAND_NAME, locale)),
                sent -> {
                    state.setMessageId(sent.getMessageId());
                    commandStateService.setState(message.getChatId(), state);
                }
        );

        return false;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        ChallengeState state = commandStateService.getState(message.getChatId(), true);
        switch (state.getState()) {
            case TEXT:
                handleTextState(state, message, text);
                break;
            case TIME:
                handleTimeState(state, message, text);
                break;
        }
    }

    @Override
    public void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {
        ChallengeState state = commandStateService.getState(callbackQuery.getMessage().getChatId(), true);
        if (requestParams.contains(Arg.COMMAND_NAME.getKey())) {
            String commandName = requestParams.getString(Arg.COMMAND_NAME.getKey());
            if (commandName.equals(CommandNames.GO_TO_NEXT_COMMAND_NAME) && state.getState().equals(ChallengeState.State.PARTICIPANTS)) {
                validateState(state);
                handleParticipantsState(state, callbackQuery);
                return;
            }
        }
        handleAddParticipant(state, callbackQuery, requestParams);
    }

    @Override
    public String getName() {
        return CommandNames.CREATE_CHALLENGE_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        return null;
    }

    @Override
    public void leave(long chatId) {
        commandStateService.deleteState(chatId);
    }

    private void handleAddParticipant(ChallengeState state, CallbackQuery callbackQuery, RequestParams requestParams) {
        state.addOrRemoveParticipant(requestParams.getInt(Arg.FRIEND_ID.getKey()));
        commandStateService.setState(callbackQuery.getMessage().getChatId(), state);

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(toMessage(state))
                        .chatId(callbackQuery.getMessage().getChatId())
                        .replyKeyboard(callbackQuery.getMessage().getReplyMarkup())
        );
    }

    private void handleParticipantsState(ChallengeState state, CallbackQuery callbackQuery) {
        CreateChallengeRequest createChallengeRequest = new CreateChallengeRequest()
                .friends(UserData.to(state.getFriends()))
                .participants(state.getParticipants())
                .reminderRequest(ReminderRequestData.to(state.getReminderRequest()))
                .challengeTime(TimeData.to(state.getTime()));

        Challenge challenge = challengeService.createChallenge(callbackQuery.getFrom(), createChallengeRequest);
        state.setState(ChallengeState.State.CREATED);
        commandStateService.setState(callbackQuery.getMessage().getChatId(), state);
        String createdMessage = challengeMessageBuilder.getChallengeCreatedDetails(callbackQuery.getFrom().getId(), challenge, new Locale(state.getUserLanguage()));
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(callbackQuery.getFrom().getId())
                        .text(createdMessage)
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .replyKeyboard(inlineKeyboardService.getChallengeCreatedKeyboard(challenge.getId(), new Locale(state.getUserLanguage())))
        );

        sendInvitations(
                challenge.getChallengeParticipants().stream()
                        .filter(challengeParticipant -> challengeParticipant.getUserId() != callbackQuery.getFrom().getId())
                        .collect(Collectors.toList()),
                challenge
        );
        commandNavigator.silentPop(callbackQuery.getMessage().getChatId());
    }

    private void handleTimeState(ChallengeState state, Message message, String text) {
        Locale locale = new Locale(state.getUserLanguage());
        ZoneId zoneId = userService.getTimeZone(message.getFrom().getId());
        Time time = timeRequestService.parseTime(text, zoneId, locale);
        validateTime(message.getFrom().getId(), time);

        state.setDuration(text);
        state.setTime(TimeData.from(time));
        List<TgUser> friends = friendshipService.getFriends(message.getFrom().getId());
        state.setFriends(UserData.from(friends));

        InlineKeyboardMarkup replyKeyboard = inlineKeyboardService.getChooseChallengeParticipantKeyboard(
                friends.stream().map(TgUser::getUserId).collect(Collectors.toList()), locale);

        String messageText = toMessage(state);
        state.setState(ChallengeState.State.PARTICIPANTS);
        commandStateService.setState(message.getChatId(), state);

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .text(messageText)
                        .chatId(message.getChatId())
                        .messageId(state.getMessageId())
                        .replyKeyboard(replyKeyboard)
        );
    }

    private void handleTextState(ChallengeState state, Message message, String text) {
        ReminderRequest reminderRequest = requestExtractor.extract(new ReminderRequestContext()
                .text(text)
                .voice(message.hasVoice())
                .creator(message.getFrom())
                .messageId(message.getMessageId()));
        validateReminderRequest(message.getFrom().getId(), reminderRequest);

        state.setChallengeName(text);
        state.setReminderRequest(ReminderRequestData.from(reminderRequest));

        String messageText = toMessage(state);
        state.setState(ChallengeState.State.TIME);
        commandStateService.setState(message.getChatId(), state);

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .text(messageText)
                        .chatId(message.getChatId())
                        .messageId(state.getMessageId())
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.START_COMMAND_NAME, new Locale(state.getUserLanguage())))
        );
    }

    private void sendInvitations(List<ChallengeParticipant> participants, Challenge challenge) {
        for (ChallengeParticipant challengeParticipant : participants) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(challengeParticipant.getUserId())
                            .text(challengeMessageBuilder.getChallengeInvitation(challenge, challengeParticipant.getUserId(), challengeParticipant.getUser().getLocale()))
                            .replyKeyboard(inlineKeyboardService.getChallengeInvitation(challenge.getId(), challengeParticipant.getUser().getLocale()))
            );
        }
    }

    private void validateState(ChallengeState state) {
        if (state.getState() == ChallengeState.State.PARTICIPANTS && state.getParticipants().isEmpty()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_PARTICIPANTS_REQUIRED, new Locale(state.getUserLanguage())));
        }
    }

    private void validateTime(int creatorId, Time time) {
        if (time.isRepeatTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_CHALLENGE_TIME, userService.getLocale(creatorId)));
        }
        if (time.isOffsetTime() && !time.getOffsetTime().getType().equals(OffsetTime.Type.FOR)) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_CHALLENGE_TIME, userService.getLocale(creatorId)));
        }
    }

    private void validateReminderRequest(int userId, ReminderRequest reminderRequest) {
        if (!reminderRequest.isRepeatTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_INCORRECT_REMINDER_TYPE_IN_CHALLENGE, userService.getLocale(userId)));
        }
    }

    private String toMessage(ChallengeState state) {
        StringBuilder message = new StringBuilder();
        Locale locale = new Locale(state.getUserLanguage());

        switch (state.getState()) {
            case TEXT:
                message
                        .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_STATE_NAME, new Object[]{state.getChallengeName()}, locale))
                        .append("\n\n")
                        .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_TIME, locale));
                break;
            case TIME:
                message
                        .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_STATE_NAME, new Object[]{state.getChallengeName()}, new Locale(state.getUserLanguage())))
                        .append("\n")
                        .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_STATE_DURATION, new Object[]{state.getDuration()}, new Locale(state.getUserLanguage())))
                        .append("\n\n")
                        .append(friendshipMessageBuilder.getFriendsList(UserData.to(state.getFriends()), MessagesProperties.MESSAGE_FRIENDS_EMPTY,
                                MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_HEADER, MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_FOOTER, locale));
                break;
            case PARTICIPANTS:
                message
                        .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_STATE_NAME, new Object[]{state.getChallengeName()}, new Locale(state.getUserLanguage())))
                        .append("\n")
                        .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_STATE_DURATION, new Object[]{state.getDuration()}, new Locale(state.getUserLanguage())))
                        .append("\n\n")
                        .append(challengeMessageBuilder.getFriendsListWithChoseParticipantsInfo(UserData.to(state.getFriends()), state.getParticipants(), locale));
                break;
        }

        return message.toString();
    }
}
