package ru.gadjini.reminder.bot.command.callback.postpone;

import ru.gadjini.reminder.bot.command.state.ReminderData;
import ru.gadjini.reminder.bot.command.state.TimeData;
import ru.gadjini.reminder.model.CallbackRequest;

public class StateData {

    private CallbackRequest callbackRequest;

    private State state;

    private ReminderData reminder;

    private TimeData postponeTime;

    public CallbackRequest getCallbackRequest() {
        return callbackRequest;
    }

    public void setCallbackRequest(CallbackRequest callbackRequest) {
        this.callbackRequest = callbackRequest;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public ReminderData getReminder() {
        return reminder;
    }

    public void setReminder(ReminderData reminder) {
        this.reminder = reminder;
    }

    public TimeData getPostponeTime() {
        return postponeTime;
    }

    public void setPostponeTime(TimeData postponeTime) {
        this.postponeTime = postponeTime;
    }

    public enum State {

        TIME,

        REASON
    }
}
