package ru.gadjini.reminder.domain.mapping;


public class ReminderMapping {

    public static final String RC_NAME = "rc_name";

    private Mapping receiverMapping;

    private Mapping creatorMapping;

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
}
