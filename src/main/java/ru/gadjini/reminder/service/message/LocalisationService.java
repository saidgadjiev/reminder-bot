package ru.gadjini.reminder.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocalisationService {

    private MessageSource messageSource;

    @Autowired
    public LocalisationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String messageCode) {
        return getMessage(messageCode, null);
    }

    public String getMessage(String messageCode, Object[] args) {
        return messageSource.getMessage(messageCode, args, Locale.getDefault());
    }
}
