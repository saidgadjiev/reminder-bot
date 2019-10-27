package ru.gadjini.reminder.domain.mapping;

import java.util.ArrayList;
import java.util.List;

public class Mapping {

    private List<String> fields = new ArrayList<>();

    public List<String> fields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
