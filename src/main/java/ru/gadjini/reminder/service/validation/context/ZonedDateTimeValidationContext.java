package ru.gadjini.reminder.service.validation.context;

import java.time.ZonedDateTime;
import java.util.Locale;

public class ZonedDateTimeValidationContext implements ValidationContext {

    private ZonedDateTime dateTime;

    private Locale locale;

    public ZonedDateTime dateTime() {
        return this.dateTime;
    }

    public Locale locale() {
        return this.locale;
    }

    public ZonedDateTimeValidationContext dateTime(final ZonedDateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public ZonedDateTimeValidationContext locale(final Locale locale) {
        this.locale = locale;
        return this;
    }


}
