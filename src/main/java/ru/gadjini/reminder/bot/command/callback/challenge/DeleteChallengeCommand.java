package ru.gadjini.reminder.bot.command.callback.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.challenge.ChallengeService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class DeleteChallengeCommand implements CallbackBotCommand {

    private ChallengeService challengeService;

    private MessageService messageService;

    @Autowired
    public DeleteChallengeCommand(ChallengeService challengeService, MessageService messageService) {
        this.challengeService = challengeService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return CommandNames.DELETE_CHALLENGE_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int challengeId = requestParams.getInt(Arg.CHALLENGE_ID.getKey());
        challengeService.deleteChallenge(callbackQuery.getFrom().getId(), challengeId);
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());

        return MessagesProperties.MESSAGE_CHALLENGE_DELETED_ANSWER;
    }
}
