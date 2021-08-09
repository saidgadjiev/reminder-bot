package ru.gadjini.reminder.bot.command.callback.goal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Goal;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.goal.GoalService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.TimeRequestService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class CreateGoalCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private TimeRequestService timeRequestService;

    private GoalService goalService;

    private MessageService messageService;

    private TgUserService userService;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    private CommandStateService commandStateService;

    @Autowired
    public CreateGoalCommand(GoalService goalService, MessageService messageService, TgUserService userService,
                             InlineKeyboardService inlineKeyboardService, LocalisationService localisationService,
                             CommandStateService commandStateService) {
        this.goalService = goalService;
        this.messageService = messageService;
        this.userService = userService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
        this.commandStateService = commandStateService;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public String getName() {
        return CommandNames.CREATE_GOAL_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        if (requestParams.contains(Arg.GOAL_ID.getKey())) {
            CreateGoalState createGoalState = new CreateGoalState();
            createGoalState.setGoalId(requestParams.getInt(Arg.GOAL_ID.getKey()));
            commandStateService.setState(callbackQuery.getFrom().getId(), createGoalState);
        }
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        String text = localisationService.getMessage(MessagesProperties.MESSAGE_SEND_GOAL, locale);
        messageService.editMessage(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_GOALS_COMMAND_NAME, locale))
                        .chatId(callbackQuery.getFrom().getId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(text)
        );

        return null;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        String[] split = text.split(";");
        Goal goal = new Goal();
        goal.setTitle(split[0]);
        goal.setDescription(split[1]);
        goal.setUserId(message.getFrom().getId());

        CreateGoalState state = commandStateService.getState(message.getChatId(), false);

        if (state != null) {
            goal.setGoalId(state.getGoalId());
        }

        ZoneId timeZone = userService.getTimeZone(message.getFrom().getId());
        Time time = timeRequestService.parseTime(split[2], timeZone, userService.getLocale(message.getFrom().getId()));

        goal.setTargetDate(time.getFixedDateTime().toZonedDateTime());

        goalService.createGoal(goal);
    }
}
