package ru.gadjini.reminder.bot.command.callback.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.challenge.ChallengeBusinessService;
import ru.gadjini.reminder.service.challenge.ChallengeMessageBuilder;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class GiveUpCommand implements CallbackBotCommand {

    private ChallengeBusinessService challengeBusinessService;

    private ChallengeMessageBuilder challengeMessageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    private MessageService messageService;

    @Autowired
    public GiveUpCommand(ChallengeBusinessService challengeBusinessService, ChallengeMessageBuilder challengeMessageBuilder,
                         InlineKeyboardService inlineKeyboardService, MessageService messageService) {
        this.challengeBusinessService = challengeBusinessService;
        this.challengeMessageBuilder = challengeMessageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return CommandNames.GIVE_UP_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int challengeId = requestParams.getInt(Arg.CHALLENGE_ID.getKey());
        Challenge challenge = challengeBusinessService.giveUp(callbackQuery.getFrom(), challengeId);
        ChallengeParticipant me = challenge.getChallengeParticipants().stream()
                .filter(challengeParticipant -> challengeParticipant.getUserId() == callbackQuery.getFrom().getId())
                .findFirst()
                .orElseThrow();

        String text = challengeMessageBuilder.getChallengeDetails(callbackQuery.getFrom().getId(), challenge, me.getUser().getLocale());
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .chatId(callbackQuery.getMessage().getChatId())
                        .text(text)
                        .replyKeyboard(inlineKeyboardService.getChallengeDetailsKeyboard(me, challenge.getCreatorId()))
        );

        return null;
    }
}
