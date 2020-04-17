package ru.gadjini.reminder.bot.command.callback.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.challenge.ChallengeBusinessService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class RejectChallengeCommand implements CallbackBotCommand {

    private ChallengeBusinessService challengeBusinessService;

    private MessageService messageService;

    @Autowired
    public RejectChallengeCommand(ChallengeBusinessService challengeBusinessService, MessageService messageService) {
        this.challengeBusinessService = challengeBusinessService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return CommandNames.REJECT_CHALLENGE_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        challengeBusinessService.rejectChallenge(callbackQuery.getFrom().getId(), requestParams.getInt(Arg.CHALLENGE_ID.getKey()));

        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());

        return MessagesProperties.MESSAGE_CHALLENGE_REJECTED_ANSWER;
    }
}
