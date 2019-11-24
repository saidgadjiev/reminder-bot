package ru.gadjini.reminder.domain;

public class RemindMessage {

    public static final String TYPE = "remind_message";

    public static final String ID = "id";

    public static final String REMINDER_ID = "reminder_id";

    public static final String MESSAGE_ID = "message_id";

    private int id;

    private int reminderId;

    private int messageId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
