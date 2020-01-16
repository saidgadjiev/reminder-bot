package ru.gadjini.reminder.service.validation;

public interface Validator {

    ValidationEvent event();

    void validate(ValidationContext validationContext);
}
