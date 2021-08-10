package ru.gadjini.reminder.bot.command.goal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.goal.GoalService;

@Component
public class CompleteGoalCommand implements CallbackBotCommand {

    private GoalService goalService;

    @Autowired
    public CompleteGoalCommand(GoalService goalService) {
        this.goalService = goalService;
    }

    @Override
    public String getName() {
        return CommandNames.COMPLETE_GOAL_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        goalService.complete(requestParams.getInt(Arg.GOAL_ID.getKey()));

        return MessagesProperties.MESSAGE_GOAL_COMPLETED_ANSWER;
    }
}
