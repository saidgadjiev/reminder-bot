package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.service.validation.context.ValidationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ValidatorFactory {

    private Map<ValidatorType, Validator<ValidationContext>> validators = new HashMap<>();

    @Autowired
    public void setValidators(Set<Validator> validatorsSet) {
        validatorsSet.forEach(validator -> validators.put(validator.event(), validator));
    }

    public Validator<ValidationContext> getValidator(ValidatorType event) {
        return validators.get(event);
    }
}
