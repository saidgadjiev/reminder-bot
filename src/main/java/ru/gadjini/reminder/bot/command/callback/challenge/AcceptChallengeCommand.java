package ru.gadjini.reminder.bot.command.callback.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.challenge.ChallengeBusinessService;
import ru.gadjini.reminder.service.challenge.ChallengeMessageBuilder;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.Locale;

@Component
public class AcceptChallengeCommand implements CallbackBotCommand {

    private ChallengeBusinessService challengeBusinessService;

    private TgUserService userService;

    private ChallengeMessageBuilder messageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    private MessageService messageService;

    @Autowired
    public AcceptChallengeCommand(ChallengeBusinessService challengeBusinessService, TgUserService userService,
                                  ChallengeMessageBuilder messageBuilder, InlineKeyboardService inlineKeyboardService,
                                  MessageService messageService) {
        this.challengeBusinessService = challengeBusinessService;
        this.userService = userService;
        this.messageBuilder = messageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return CommandNames.ACCEPT_CHALLENGE_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Challenge challenge = challengeBusinessService.acceptChallenge(callbackQuery.getFrom(), requestParams.getInt(Arg.CHALLENGE_ID.getKey()));
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        String challengeDetails = messageBuilder.getChallengeCreatedDetails(callbackQuery.getFrom().getId(), MessagesProperties.MESSAGE_CHALLENGE, challenge, locale);

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(callbackQuery.getFrom().getId())
                        .text(challengeDetails)
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_CHALLENGES_COMMAND_NAME, locale))
        );

        return MessagesProperties.MESSAGE_CHALLENGE_ACCEPTED_ANSWER;
    }
}
