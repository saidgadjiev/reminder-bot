package ru.gadjini.reminder.domain.mapping;

import java.util.List;

public class FriendshipMapping {

    public static final String UO_NAME = "uo_name";

    private Mapping userOneMapping;

    private Mapping userTwoMapping;

    private List<String> fields;

    public void setUserOneMapping(Mapping userOneMapping) {
        this.userOneMapping = userOneMapping;
    }

    public Mapping getUserOneMapping() {
        return userOneMapping;
    }

    public Mapping getUserTwoMapping() {
        return userTwoMapping;
    }

    public void setUserTwoMapping(Mapping userTwoMapping) {
        this.userTwoMapping = userTwoMapping;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<String> getFields() {
        return fields;
    }
}
