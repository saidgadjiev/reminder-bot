package ru.gadjini.reminder.bot.command.callback.cancel;

import ru.gadjini.reminder.bot.command.callback.state.ReminderData;
import ru.gadjini.reminder.model.CallbackRequest;

public class StateData {

    private CallbackRequest callbackRequest;

    private State state;

    private ReminderData reminder;

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

    public enum State {

        REASON
    }
}
