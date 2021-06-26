package ru.gadjini.reminder.bot.command.callback.tag;

public class TagState {

    private int reminderId;

    private int messageId;

    public TagState() {

    }

    public TagState(int reminderId, int messageId) {
        this.reminderId = reminderId;
        this.messageId = messageId;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
