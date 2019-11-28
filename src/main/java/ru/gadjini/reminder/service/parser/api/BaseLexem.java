package ru.gadjini.reminder.service.parser.api;

public abstract class BaseLexem {

    private String value;

    public BaseLexem(String value) {
        this.value = value;
    }

    public final String getValue() {
        return value;
    }

    public abstract Token getToken();
}
