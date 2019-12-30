package ru.gadjini.reminder.domain.mapping;


public class ReminderMapping {

    public static final String RC_NAME = "rc_name";

    public static final String RC_CHAT_ID = "rc_chat_id";

    public static final String CR_CHAT_ID = "cr_chat_id";

    private Mapping receiverMapping;

    private Mapping creatorMapping;

    private Mapping remindMessageMapping;

    public Mapping getReceiverMapping() {
        return receiverMapping;
    }

    public ReminderMapping setReceiverMapping(Mapping userMapping) {
        this.receiverMapping = userMapping;

        return this;
    }

    public Mapping getCreatorMapping() {
        return creatorMapping;
    }

    public ReminderMapping setCreatorMapping(Mapping creatorMapping) {
        this.creatorMapping = creatorMapping;

        return this;
    }

    public Mapping getRemindMessageMapping() {
        return remindMessageMapping;
    }

    public ReminderMapping setRemindMessageMapping(Mapping remindMessageMapping) {
        this.remindMessageMapping = remindMessageMapping;

        return this;
    }
}
