package ru.gadjini.reminder.service.validation.context;

import java.util.Locale;

public class UserValidationContext implements ValidationContext {

    private String username;

    private Long userId;

    private Locale locale;

    public String username() {
        return this.username;
    }

    public Long userId() {
        return this.userId;
    }

    public UserValidationContext username(final String username) {
        this.username = username;
        return this;
    }

    public UserValidationContext userId(final Long userId) {
        this.userId = userId;
        return this;
    }

    public Locale locale() {
        return this.locale;
    }

    public UserValidationContext locale(final Locale locale) {
        this.locale = locale;
        return this;
    }


}
