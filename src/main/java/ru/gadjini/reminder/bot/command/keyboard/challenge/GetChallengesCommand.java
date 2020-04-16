package ru.gadjini.reminder.bot.command.keyboard.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.challenge.ChallengeMessageBuilder;
import ru.gadjini.reminder.service.challenge.ChallengeService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GetChallengesCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private Set<String> names = new HashSet<>();

    private MessageService messageService;

    private ChallengeMessageBuilder challengeMessageBuilder;

    private ChallengeService challengeService;

    private TgUserService userService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public GetChallengesCommand(LocalisationService localisationService, MessageService messageService,
                                ChallengeMessageBuilder challengeMessageBuilder, ChallengeService challengeService,
                                TgUserService userService, InlineKeyboardService inlineKeyboardService) {
        this.messageService = messageService;
        this.challengeMessageBuilder = challengeMessageBuilder;
        this.challengeService = challengeService;
        this.userService = userService;
        this.inlineKeyboardService = inlineKeyboardService;
        for (Locale locale : localisationService.getSupportedLocales()) {
            names.add(localisationService.getMessage(MessagesProperties.GET_CHALLENGES_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<Challenge> userChallenges = challengeService.getUserChallenges(message.getFrom().getId());

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(message.getChatId())
                        .text(challengeMessageBuilder.getUserChallenges(userChallenges, userService.getLocale(message.getFrom().getId())))
                        .replyKeyboard(inlineKeyboardService.getUserChallengesKeyboard(userChallenges.stream().map(Challenge::getId).collect(Collectors.toList())))
        );

        return false;
    }

    @Override
    public String getName() {
        return CommandNames.GET_CHALLENGES_COMMAND_NAME;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<Challenge> userChallenges = challengeService.getUserChallenges(tgMessage.getUser().getId());

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .messageId(tgMessage.getMessageId())
                        .chatId(tgMessage.getChatId())
                        .text(challengeMessageBuilder.getUserChallenges(userChallenges, userService.getLocale(tgMessage.getUser().getId())))
                        .replyKeyboard(inlineKeyboardService.getUserChallengesKeyboard(userChallenges.stream().map(Challenge::getId).collect(Collectors.toList())))
        );
    }
}
