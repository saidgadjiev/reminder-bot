package ru.gadjini.reminder.service.parser.reminder.parser;

import java.time.ZonedDateTime;

public class ParsedRequest {

    private String receiverName;

    private String text;

    private String note;

    private ZonedDateTime parsedTime;

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

    public ZonedDateTime getParsedTime() {
        return parsedTime;
    }

    void setParsedTime(ZonedDateTime parsedTime) {
        this.parsedTime = parsedTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
