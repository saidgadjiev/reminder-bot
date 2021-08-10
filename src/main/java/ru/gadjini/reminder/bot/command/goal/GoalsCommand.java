package ru.gadjini.reminder.bot.command.goal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Goal;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
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

    private LocalisationService localisationService;

    @Autowired
    public GoalsCommand(GoalService goalService, MessageService messageService,
                        InlineKeyboardService inlineKeyboardService, LocalisationService localisationService,
                        TgUserService userService) {
        this.goalService = goalService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
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
        Integer goalId = requestParams.getInt(Arg.GOAL_ID.getKey());
        List<Goal> goals = goalService.getGoals(callbackQuery.getFrom().getId(), goalId);
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        String msg = buildMessage(goals, locale);

        messageService.editMessage(new EditMessageContext(PriorityJob.Priority.HIGH)
                .text(msg)
                .chatId(callbackQuery.getFrom().getId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .replyKeyboard(inlineKeyboardService.goalsKeyboard(goalId, goals, locale)));

        return null;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<Goal> goals = goalService.getGoals(message.getFrom().getId());
        Locale locale = userService.getLocale(message.getFrom().getId());
        String msg = buildMessage(goals, locale);

        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH)
                .text(msg)
                .chatId(message.getChatId())
                .html(true)
                .replyKeyboard(inlineKeyboardService.goalsKeyboard(goals, locale)));

        return false;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<Goal> goals = goalService.getGoals(tgMessage.getUser().getId());
        Locale locale = userService.getLocale(tgMessage.getUser().getId());
        String msg = buildMessage(goals, locale);

        messageService.editMessageAsync(new EditMessageContext(PriorityJob.Priority.HIGH)
                .text(msg)
                .chatId(tgMessage.getChatId())
                .messageId(tgMessage.getMessageId())
                .replyKeyboard(inlineKeyboardService.goalsKeyboard(goals, locale)));
    }

    private String buildMessage(List<Goal> goals, Locale locale) {
        StringBuilder message = new StringBuilder();
        int i = 1;
        for (Goal goal : goals) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ");
            if (goal.isCompleted()) {
                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_CHECK_ICON, locale));
            }
            message.append(goal.getTitle());
        }
        if (message.length() == 0) {
            return "No goals";
        }

        return message.toString();
    }
}
