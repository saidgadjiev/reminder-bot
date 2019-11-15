package ru.gadjini.reminder.service.parser.reminder.parser;

public class ParsedRequest {

    private String receiverName;

    private String text;

    private String note;

    private ParsedTime parsedTime;

    public String getReceiverName() {
        return receiverName;
    }

    void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    public ParsedTime getParsedTime() {
        return parsedTime;
    }

    void setParsedTime(ParsedTime parsedTime) {
        this.parsedTime = parsedTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
