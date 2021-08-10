package ru.gadjini.reminder.bot.command.goal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Goal;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.goal.GoalService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.time.Time2TextService;

import java.time.ZoneId;
import java.util.Locale;

@Component
public class GoalDetailsCommand implements CallbackBotCommand {

    private MessageService messageService;

    private GoalService goalService;

    private Time2TextService time2TextService;

    private TgUserService userService;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    @Autowired
    public GoalDetailsCommand(MessageService messageService, GoalService goalService,
                              Time2TextService time2TextService, TgUserService userService,
                              InlineKeyboardService inlineKeyboardService, LocalisationService localisationService) {
        this.messageService = messageService;
        this.goalService = goalService;
        this.time2TextService = time2TextService;
        this.userService = userService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
    }

    @Override
    public String getName() {
        return CommandNames.GOAL_DETAILS_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Goal goal = goalService.getGoal(requestParams.getInt(Arg.GOAL_ID.getKey()));

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessage(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(callbackQuery.getFrom().getId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(buildMessage(goal))
                        .replyKeyboard(inlineKeyboardService.goalDetails(goal.getId(), locale))
        );

        return null;
    }

    private String buildMessage(Goal goal) {
        Locale locale = userService.getLocale(goal.getUserId());
        StringBuilder message = new StringBuilder();
        if (goal.isCompleted()) {
            message.append(localisationService.getMessage(MessagesProperties.MESSAGE_CHECK_ICON, locale));
        }
        message.append("<b>").append(goal.getTitle()).append("</b>\n\n");

        message.append(goal.getDescription()).append("\n\n");

        ZoneId zoneId = userService.getTimeZone(goal.getUserId());
        message.append("<b>Created at:</b> ").append(time2TextService.time(goal.getCreatedAt().withZoneSameInstant(zoneId), locale)).append("\n");
        message.append("<b>Target date:</b> ").append(time2TextService.time(goal.getTargetDate(), zoneId, locale));

        return message.toString();
    }
}
