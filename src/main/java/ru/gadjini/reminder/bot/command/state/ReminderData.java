package ru.gadjini.reminder.bot.command.state;

import ru.gadjini.reminder.domain.Reminder;

public class ReminderData {

    private int id;

    private String text;

    private int creatorId;

    private UserData creator;

    private int receiverId;

    private UserData receiver;

    private DateTimeData remindAt;

    private String note;

    private Integer creatorMessageId;

    private Integer receiverMessageId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public UserData getCreator() {
        return creator;
    }

    public void setCreator(UserData creator) {
        this.creator = creator;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public UserData getReceiver() {
        return receiver;
    }

    public void setReceiver(UserData receiver) {
        this.receiver = receiver;
    }

    public DateTimeData getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(DateTimeData remindAt) {
        this.remindAt = remindAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getCreatorMessageId() {
        return creatorMessageId;
    }

    public void setCreatorMessageId(Integer creatorMessageId) {
        this.creatorMessageId = creatorMessageId;
    }

    public Integer getReceiverMessageId() {
        return receiverMessageId;
    }

    public void setReceiverMessageId(Integer receiverMessageId) {
        this.receiverMessageId = receiverMessageId;
    }

    public static Reminder to(ReminderData reminderData) {
        Reminder reminder = new Reminder();

        reminder.setId(reminderData.getId());
        reminder.setText(reminderData.getText());
        reminder.setCreatorId(reminderData.getCreatorId());
        reminder.setCreator(UserData.to(reminderData.getCreator()));
        reminder.setReceiverId(reminderData.getReceiverId());
        reminder.setReceiver(UserData.to(reminderData.getReceiver()));
        reminder.setNote(reminderData.getNote());
        reminder.setRemindAt(DateTimeData.to(reminderData.getRemindAt()));
        reminder.setCreatorMessageId(reminderData.getCreatorMessageId());
        reminder.setReceiverMessageId(reminderData.getReceiverMessageId());

        return reminder;
    }

    public static ReminderData from(Reminder reminder) {
        ReminderData reminderData = new ReminderData();

        reminderData.id = reminder.getId();
        reminderData.text = reminder.getText();
        reminderData.creatorId = reminder.getCreatorId();
        reminderData.creator = UserData.from(reminder.getCreator());
        reminderData.receiverId = reminder.getReceiverId();
        reminderData.receiver = UserData.from(reminder.getReceiver());
        reminderData.note = reminder.getNote();
        reminderData.remindAt = DateTimeData.from(reminder.getRemindAt());
        reminderData.creatorMessageId = reminder.getCreatorMessageId();
        reminderData.receiverMessageId = reminder.getReceiverMessageId();

        return reminderData;
    }
}
