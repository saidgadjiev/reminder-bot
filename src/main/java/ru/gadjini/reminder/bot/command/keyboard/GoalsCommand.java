package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Goal;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.goal.GoalService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class GoalsCommand implements KeyboardBotCommand, NavigableCallbackBotCommand, CallbackBotCommand {

    private Set<String> names = new HashSet<>();

    private GoalService goalService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private TgUserService userService;

    @Autowired
    public GoalsCommand(GoalService goalService, MessageService messageService,
                        InlineKeyboardService inlineKeyboardService, LocalisationService localisationService,
                        TgUserService userService) {
        this.goalService = goalService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.userService = userService;

        for (Locale supportedLocale : localisationService.getSupportedLocales()) {
            names.add(localisationService.getMessage(MessagesProperties.GOALS_COMMAND_DESCRIPTION, supportedLocale));
        }
    }

    @Override
    public String getName() {
        return CommandNames.GET_GOALS_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        List<Goal> goals = requestParams.contains(Arg.GOAL_ID.getKey())
                ? goalService.getGoals(callbackQuery.getFrom().getId(), requestParams.getInt(Arg.GOAL_ID.getKey()))
                : goalService.getGoals(callbackQuery.getFrom().getId());
        String msg = buildMessage(goals);
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());

        messageService.editMessage(new EditMessageContext(PriorityJob.Priority.HIGH)
                .text(msg)
                .chatId(callbackQuery.getFrom().getId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .replyKeyboard(inlineKeyboardService.goalsKeyboard(goals, locale)));

        return null;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<Goal> goals = goalService.getGoals(message.getFrom().getId());
        String msg = buildMessage(goals);
        Locale locale = userService.getLocale(message.getFrom().getId());

        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH)
                .text(msg)
                .chatId(message.getChatId())
                .html(true)
                .replyKeyboard(inlineKeyboardService.goalsKeyboard(goals, locale)));

        return false;
    }

    private String buildMessage(List<Goal> goals) {
        StringBuilder message = new StringBuilder();
        int i = 1;
        for (Goal goal : goals) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i).append(") ").append(goal.getTitle());
        }
        if (message.length() == 0) {
            return "No goals";
        }

        return message.toString();
    }
}
