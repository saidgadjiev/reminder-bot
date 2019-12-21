package ru.gadjini.reminder.service.validation;

import ru.gadjini.reminder.domain.time.Time;

public interface Validator {

    ValidationEvent event();

    void validate(Time time);
}
