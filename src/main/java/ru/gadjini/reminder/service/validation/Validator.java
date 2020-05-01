package ru.gadjini.reminder.service.validation;

import ru.gadjini.reminder.service.validation.context.ValidationContext;

public interface Validator<T extends ValidationContext> {

    ValidatorType event();

    void validate(T validationContext);
}
