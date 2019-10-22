package ru.gadjini.reminder.service.validation;

import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorBag {

    private Map<String, String> errors = new LinkedHashMap<>();

    public void set(String field, String message) {
        errors.put(field, message);
    }

    public boolean hasError(String field) {
        return errors.containsKey(field);
    }

    public String getErrorMessage(String field) {
        return errors.get(field);
    }

    public String firstErrorMessage() {
        return errors.values().iterator().next();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
