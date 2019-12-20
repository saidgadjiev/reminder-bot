package ru.gadjini.reminder.service.parser.reminder.parser;

import ru.gadjini.reminder.domain.OffsetTime;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.domain.Time;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;

public class ReminderRequest {

    private String receiverName;

    private String text;

    private String note;

    private Time time;

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public DateTime getFixedTime() {
        return time.getFixedTime();
    }

    void setFixedTime(DateTime fixedTime) {
        time.setFixedTime(fixedTime);
    }

    public RepeatTime getRepeatTime() {
        return time.getRepeatTime();
    }

    public void setRepeatTime(RepeatTime repeatTime) {
        time.setRepeatTime(repeatTime);
    }

    public OffsetTime getOffsetTime() {
        return time.getOffsetTime();
    }

    public void setOffsetTime(OffsetTime offsetTime) {
        time.setOffsetTime(offsetTime);
    }

    public ZoneId getZone() {
        return time.getZoneId();
    }

    public boolean isRepeatTime() {
        return time.isRepeatTime();
    }

    public boolean isFixedTime() {
        return time.isFixedTime();
    }

    public boolean isOffsetTime() {
        return time.isOffsetTime();
    }
}
