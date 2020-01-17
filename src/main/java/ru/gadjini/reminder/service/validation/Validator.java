package ru.gadjini.reminder.service.validation;

public interface Validator {

    ValidatorType event();

    void validate(ValidationContext validationContext);
}
