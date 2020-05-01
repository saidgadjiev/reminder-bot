package ru.gadjini.reminder.service.validation.context;

import ru.gadjini.reminder.domain.time.Time;

import java.util.Locale;

public class TimeValidationContext implements ValidationContext {

    private Time time;

    private Locale locale;

    public Time time() {
        return this.time;
    }

    public TimeValidationContext time(final Time time) {
        this.time = time;
        return this;
    }

    public Locale locale() {
        return this.locale;
    }

    public TimeValidationContext locale(final Locale locale) {
        this.locale = locale;
        return this;
    }
}
