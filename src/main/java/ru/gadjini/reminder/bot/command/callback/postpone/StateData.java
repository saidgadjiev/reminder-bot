package ru.gadjini.reminder.bot.command.callback.postpone;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import ru.gadjini.reminder.model.CallbackRequest;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class StateData {

    private CallbackRequest callbackRequest;

    private PostponeReminderCommand.State state;

    private ReminderData reminder;

    private TimeData postponeTime;

    public CallbackRequest getCallbackRequest() {
        return callbackRequest;
    }

    public void setCallbackRequest(CallbackRequest callbackRequest) {
        this.callbackRequest = callbackRequest;
    }

    public PostponeReminderCommand.State getState() {
        return state;
    }

    public void setState(PostponeReminderCommand.State state) {
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
}
