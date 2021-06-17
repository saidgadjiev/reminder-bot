package ru.gadjini.reminder.bot.command.state;

import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

import java.time.ZoneId;
import java.util.Locale;

public class ReminderRequestData {

    private String receiverName;

    private Long receiverId;

    private Long creatorId;

    private String text;

    private String note;

    private TimeData time;

    private int messageId;

    private String language;

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
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

    public ZoneId getZone() {
        return time.getZoneId();
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public TimeData getTime() {
        return time;
    }

    public void setTime(TimeData time) {
        this.time = time;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public static ReminderRequest to(ReminderRequestData reminderRequestData) {
        ReminderRequest reminderRequest = new ReminderRequest();
        reminderRequest.setReceiverName(reminderRequestData.getReceiverName());
        reminderRequest.setLocale(reminderRequestData.getLanguage() != null ? new Locale(reminderRequestData.getLanguage()) : null);
        reminderRequest.setNote(reminderRequestData.getNote());
        reminderRequest.setReceiverId(reminderRequestData.getReceiverId());
        reminderRequest.setText(reminderRequestData.getText());
        reminderRequest.setTime(TimeData.to(reminderRequestData.getTime()));

        return reminderRequest;
    }

    public static ReminderRequestData from(ReminderRequest reminderRequest) {
        ReminderRequestData requestData = new ReminderRequestData();
        requestData.setReceiverName(reminderRequest.getReceiverName());
        requestData.setLanguage(reminderRequest.getLocale().getLanguage());
        requestData.setNote(reminderRequest.getNote());
        requestData.setReceiverId(reminderRequest.getReceiverId());
        requestData.setText(reminderRequest.getText());
        requestData.setTime(TimeData.from(reminderRequest.getTime()));

        return requestData;
    }
}
