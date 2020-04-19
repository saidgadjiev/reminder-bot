package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.challenge.ChallengeMessageBuilder;
import ru.gadjini.reminder.service.challenge.ChallengeService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.Locale;

@Component
public class ChallengeDetailsCommand implements CallbackBotCommand {

    private ChallengeMessageBuilder messageBuilder;

    private ChallengeService challengeService;

    private TgUserService userService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public ChallengeDetailsCommand(ChallengeMessageBuilder messageBuilder, ChallengeService challengeService,
                                   TgUserService userService, MessageService messageService, InlineKeyboardService inlineKeyboardService) {
        this.messageBuilder = messageBuilder;
        this.challengeService = challengeService;
        this.userService = userService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public String getName() {
        return CommandNames.CHALLENGE_DETAILS_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int challengeId = requestParams.getInt(Arg.CHALLENGE_ID.getKey());
        Challenge challenge = challengeService.getChallenge(challengeId);
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        String challengeDetails = messageBuilder.getChallengeDetails(callbackQuery.getFrom().getId(), challenge, locale);

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(callbackQuery.getFrom().getId())
                        .text(challengeDetails)
                        .messageId(callbackQuery.getMessage().getMessageId())
                .replyKeyboard(inlineKeyboardService.getChallengeDetailsKeyboard(challengeId, locale))
        );

        return null;
    }
}
