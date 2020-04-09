package ru.gadjini.reminder.bot.command.keyboard.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.bot.command.state.ReminderRequestData;
import ru.gadjini.reminder.bot.command.state.TimeData;
import ru.gadjini.reminder.bot.command.state.UserData;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CreateChallengeRequest;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.challenge.ChallengeMessageBuilder;
import ru.gadjini.reminder.service.challenge.ChallengeService;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.reminder.TimeRequestService;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestExtractor;
import ru.gadjini.reminder.util.UserUtils;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CreateChallengeCommand implements KeyboardBotCommand, NavigableBotCommand, CallbackBotCommand {

    private Set<String> names = new HashSet<>();

    private final LocalisationService localisationService;

    private MessageService messageService;

    private TgUserService userService;

    private ReplyKeyboardService replyKeyboardService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderRequestExtractor requestExtractor;

    private CommandStateService commandStateService;

    private TimeRequestService timeRequestService;

    private FriendshipService friendshipService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private ChallengeService challengeService;

    private ChallengeMessageBuilder challengeMessageBuilder;

    @Autowired
    public CreateChallengeCommand(LocalisationService localisationService, MessageService messageService,
                                  TgUserService userService, @Qualifier("currkeyboard") ReplyKeyboardService replyKeyboardService,
                                  InlineKeyboardService inlineKeyboardService, @Qualifier("chain") ReminderRequestExtractor requestExtractor,
                                  CommandStateService commandStateService, TimeRequestService timeRequestService,
                                  FriendshipService friendshipService, FriendshipMessageBuilder friendshipMessageBuilder,
                                  ChallengeService challengeService, ChallengeMessageBuilder challengeMessageBuilder) {
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.userService = userService;
        this.replyKeyboardService = replyKeyboardService;
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

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        ChallengeState state = new ChallengeState();
        Locale locale = userService.getLocale(message.getFrom().getId());
        state.setUserLanguage(locale.getLanguage());
        commandStateService.setState(message.getChatId(), state);

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CREATE_CHALLENGE, locale))
                        .replyKeyboard(replyKeyboardService.goBackCommand(message.getChatId(), locale))
        );

        return true;
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
            case PARTICIPANTS:
                if (isNextText(text, new Locale(state.getUserLanguage()))) {
                    validateState(state);
                    handleParticipantsState(state, message);
                }

                break;
        }
    }

    @Override
    public String getHistoryName() {
        return CommandNames.CREATE_CHALLENGE_COMMAND_NAME;
    }

    @Override
    public void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {
        ChallengeState state = commandStateService.getState(callbackQuery.getMessage().getChatId(), true);
        state.addOrRemoveParticipant(requestParams.getInt(Arg.FRIEND_ID.getKey()));

        Locale locale = new Locale(state.getUserLanguage());
        String friendsList = friendshipMessageBuilder.getFriendsList(UserData.to(state.getFriends()), MessagesProperties.MESSAGE_FRIENDS_EMPTY,
                MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_HEADER, getFooterWithChoseParticipants(state.getFriends(), state.getParticipants(), locale), locale);
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .text(friendsList)
                        .chatId(callbackQuery.getMessage().getChatId())
                        .replyKeyboard(callbackQuery.getMessage().getReplyMarkup())
        );
    }

    @Override
    public String getName() {
        return CommandNames.CREATE_CHALLENGE_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        return null;
    }

    private String getFooterWithChoseParticipants(List<UserData> participants, Set<Integer> choseParticipants, Locale locale) {
        StringBuilder selectedParticipants = new StringBuilder();

        for (UserData userData : participants) {
            if (selectedParticipants.length() > 0) {
                selectedParticipants.append(", ");
            }
            if (choseParticipants.contains(userData.getUserId())) {
                selectedParticipants.append(UserUtils.userLink(userData.getUserId(), userData.getName()));
            }
        }

        return localisationService.getMessage(MessagesProperties.MESSAGE_CHOSE_PARTICIPANTS, new Object[]{selectedParticipants.toString()}, locale) + "\"" +
                localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_FOOTER, locale);
    }

    private void handleParticipantsState(ChallengeState state, Message message) {
        CreateChallengeRequest createChallengeRequest = new CreateChallengeRequest()
                .friends(UserData.to(state.getFriends()))
                .participants(state.getParticipants())
                .reminderRequest(ReminderRequestData.to(state.getReminderRequest()))
                .challengeTime(TimeData.to(state.getTime()));

        Challenge challenge = challengeService.createChallenge(message.getFrom(), createChallengeRequest);
        String createdMessage = challengeMessageBuilder.getChallengeCreated(challenge, new Locale(state.getUserLanguage()));
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(message.getChatId())
                        .text(createdMessage)
        );
    }

    private void handleTimeState(ChallengeState state, Message message, String text) {
        Locale locale = new Locale(state.getUserLanguage());
        ZoneId zoneId = userService.getTimeZone(message.getFrom().getId());
        Time time = timeRequestService.parseTime(text, zoneId, locale);
        state.setTime(TimeData.from(time));
        state.setState(ChallengeState.State.PARTICIPANTS);
        List<TgUser> friends = friendshipService.getFriends(message.getFrom().getId());
        state.setFriends(UserData.from(friends));
        commandStateService.setState(message.getChatId(), state);

        String friendsList = friendshipMessageBuilder.getFriendsList(friends, MessagesProperties.MESSAGE_FRIENDS_EMPTY,
                MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_HEADER, MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_FOOTER, locale);
        ReplyKeyboard replyKeyboard = inlineKeyboardService.getFriendsListKeyboard(friends.stream().map(TgUser::getUserId).collect(Collectors.toList()), CommandNames.CALLBACK_DELEGATE_COMMAND_NAME);
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .text(friendsList)
                        .chatId(message.getChatId())
                        .replyKeyboard(replyKeyboard)
        );
    }

    private void handleTextState(ChallengeState state, Message message, String text) {
        ReminderRequest reminderRequest = requestExtractor.extract(new ReminderRequestContext()
                .text(text)
                .voice(message.hasVoice())
                .creator(message.getFrom())
                .messageId(message.getMessageId()));
        state.setReminderRequest(ReminderRequestData.from(reminderRequest));
        state.setState(ChallengeState.State.TIME);
        commandStateService.setState(message.getChatId(), state);

        Locale locale = new Locale(state.getUserLanguage());
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_TIME, locale))
                        .chatId(message.getChatId())
        );
    }

    private void validateState(ChallengeState state) {
        if (state.getState() == ChallengeState.State.PARTICIPANTS && state.getParticipants().isEmpty()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_PARTICIPANTS_REQUIRED, new Locale(state.getUserLanguage())));
        }
    }

    private boolean isNextText(String text, Locale locale) {
        return localisationService.getMessage(MessagesProperties.GOTO_NEXT_COMMAND_NAME, locale).equals(text);
    }
}
