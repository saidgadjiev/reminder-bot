package ru.gadjini.reminder.service.parser.remind.parser;

import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.time.DateTime;

public class CustomRemindTime {

    private RepeatTime repeatTime;

    private DateTime time;

    private OffsetTime offsetTime;

    public RepeatTime getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(RepeatTime repeatTime) {
        this.repeatTime = repeatTime;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public OffsetTime getRemindTime() {
        return offsetTime;
    }

    public void setOffsetTime(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }

    public boolean isRepeatTime() {
        return repeatTime != null;
    }

    public boolean isStandardTime() {
        return time != null;
    }

    public boolean getOffsetTime() {
        return offsetTime != null;
    }
}
