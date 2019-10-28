package ru.gadjini.reminder.domain.mapping;

import java.util.ArrayList;
import java.util.List;

public class ReminderMapping {

    public static final String RM_MESSAGE = "rm_message";

    public static final String RC_FIRST_LAST_NAME = "rc_first_last_name";

    public static final String RC_CHAT_ID = "rc_chat_id";

    public static final String CR_CHAT_ID = "cr_chat_id";

    private Mapping receiverMapping;

    private Mapping creatorMapping;

    private List<String> fields = new ArrayList<>();

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

    public List<String> fields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
