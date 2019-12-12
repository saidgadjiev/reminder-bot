package ru.gadjini.reminder.service.parser.reminder.parser;

import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;

public class ParsedRequest {

    private String receiverName;

    private String text;

    private String note;

    private DateTime parsedTime;

    private RepeatTime repeatTime;

    private ZoneId zone;

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

    public DateTime getParsedTime() {
        return parsedTime;
    }

    void setParsedTime(DateTime parsedTime) {
        this.parsedTime = parsedTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public RepeatTime getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(RepeatTime repeatTime) {
        this.repeatTime = repeatTime;
    }

    public boolean isRepeatReminder() {
        return repeatTime != null;
    }

    public ZoneId getZone() {
        return zone;
    }

    public void setZone(ZoneId zone) {
        this.zone = zone;
    }
}
