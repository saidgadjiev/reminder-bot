package ru.gadjini.reminder.exception;

import ru.gadjini.reminder.service.validation.ErrorBag;

public class ValidationException extends RuntimeException {

    private ErrorBag errorBag;

    public ValidationException(ErrorBag errorBag) {
        super(errorBag.firstErrorMessage());
        this.errorBag = errorBag;
    }

    public ErrorBag getErrorBag() {
        return errorBag;
    }
}
