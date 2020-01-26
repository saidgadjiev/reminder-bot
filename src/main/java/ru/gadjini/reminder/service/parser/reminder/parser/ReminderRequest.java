package ru.gadjini.reminder.service.parser.reminder.parser;

import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;

public class ReminderRequest {

    private String receiverName;

    private Integer receiverId;

    private String text;

    private String note;

    private Time time;

    private int messageId;

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

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
        return time.getFixedDateTime();
    }

    public void setFixedTime(FixedTime fixedTime) {
        time.setFixedTime(fixedTime);
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

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
