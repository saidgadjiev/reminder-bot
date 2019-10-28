package ru.gadjini.reminder.service.resolver.parser;

public class ParsedRequest {

    private String receiverName;

    private String text;

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
}
