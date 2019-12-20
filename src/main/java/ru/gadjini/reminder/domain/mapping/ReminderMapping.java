package ru.gadjini.reminder.domain.mapping;


public class ReminderMapping {

    public static final String RC_FIRST_LAST_NAME = "rc_first_last_name";

    public static final String RC_CHAT_ID = "rc_chat_id";

    public static final String CR_CHAT_ID = "cr_chat_id";

    private Mapping receiverMapping;

    private Mapping creatorMapping;

    private Mapping remindMessageMapping;

    public Mapping getReceiverMapping() {
        return receiverMapping;
    }

    public void setReceiverMapping(Mapping userMapping) {
        this.receiverMapping = userMapping;
    }

    public Mapping getCreatorMapping() {
        return creatorMapping;
    }

    public void setCreatorMapping(Mapping creatorMapping) {
        this.creatorMapping = creatorMapping;
    }

    public Mapping getRemindMessageMapping() {
        return remindMessageMapping;
    }

    public void setRemindMessageMapping(Mapping remindMessageMapping) {
        this.remindMessageMapping = remindMessageMapping;
    }
}
