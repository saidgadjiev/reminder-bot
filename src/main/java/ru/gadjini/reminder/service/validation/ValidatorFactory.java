package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ValidatorFactory {

    private Map<ValidationEvent, Validator> validators = new HashMap<>();

    @Autowired
    public ValidatorFactory(Set<Validator> validatorsSet) {
        validatorsSet.forEach(validator -> validators.put(validator.event(), validator));
    }

    public Validator getValidator(ValidationEvent event) {
        return validators.get(event);
    }
}
