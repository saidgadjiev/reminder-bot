package ru.gadjini.reminder.bot.command.callback.challenge;

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
import ru.gadjini.reminder.service.challenge.ChallengeBusinessService;
import ru.gadjini.reminder.service.challenge.ChallengeMessageBuilder;
import ru.gadjini.reminder.service.challenge.ChallengeService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExitCommand implements CallbackBotCommand {

    private ChallengeBusinessService challengeBusinessService;

    private ChallengeService challengeService;

    private ChallengeMessageBuilder challengeMessageBuilder;

    private TgUserService userService;

    private InlineKeyboardService inlineKeyboardService;

    private MessageService messageService;

    @Autowired
    public ExitCommand(ChallengeBusinessService challengeBusinessService, ChallengeService challengeService,
                       ChallengeMessageBuilder challengeMessageBuilder, TgUserService userService,
                       InlineKeyboardService inlineKeyboardService, MessageService messageService) {
        this.challengeBusinessService = challengeBusinessService;
        this.challengeService = challengeService;
        this.challengeMessageBuilder = challengeMessageBuilder;
        this.userService = userService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return CommandNames.EXIT_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int challengeId = requestParams.getInt(Arg.CHALLENGE_ID.getKey());
        challengeBusinessService.exit(callbackQuery.getFrom(), challengeId);
        List<Challenge> userChallenges = challengeService.getUserChallenges(callbackQuery.getFrom().getId());

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .chatId(callbackQuery.getMessage().getChatId())
                        .text(challengeMessageBuilder.getUserChallenges(userChallenges, userService.getLocale(callbackQuery.getFrom().getId())))
                        .replyKeyboard(inlineKeyboardService.getUserChallengesKeyboard(userChallenges.stream().map(Challenge::getId).collect(Collectors.toList())))
        );

        return null;
    }
}
