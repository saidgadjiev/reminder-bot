package ru.gadjini.reminder.model;

public class SendFriendRequestRequest {

    private int initiatorUserId;

    private String receiverUsername;

    public int getInitiatorUserId() {
        return initiatorUserId;
    }

    public void setInitiatorUserId(int initiatorUserId) {
        this.initiatorUserId = initiatorUserId;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }
}
