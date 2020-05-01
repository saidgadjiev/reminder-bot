package ru.gadjini.reminder.service.validation.context;

import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.time.DateTime;

import java.util.Locale;

public class ReminderTimeValidationContext implements ValidationContext {

    private DateTime remindAt;

    private Time time;

    private Locale locale;

    private Integer challengeId;

    public DateTime remindAt() {
        return this.remindAt;
    }

    public Time time() {
        return this.time;
    }

    public Locale locale() {
        return this.locale;
    }

    public ReminderTimeValidationContext remindAt(final DateTime remindAt) {
        this.remindAt = remindAt;
        return this;
    }

    public ReminderTimeValidationContext time(final Time time) {
        this.time = time;
        return this;
    }

    public ReminderTimeValidationContext locale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    public Integer challengeId() {
        return this.challengeId;
    }

    public ReminderTimeValidationContext challengeId(final Integer challengeId) {
        this.challengeId = challengeId;
        return this;
    }


}
