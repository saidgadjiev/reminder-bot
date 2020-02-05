package ru.gadjini.reminder.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.service.context.UserContextResolver;

import java.util.List;
import java.util.Locale;

@Service
public class LocalisationService {

    private MessageSource messageSource;

    private UserContextResolver userContextResolver;

    private static final String RU_LOCALE = "ru";

    @Autowired
    public LocalisationService(MessageSource messageSource, UserContextResolver userContextResolver) {
        this.messageSource = messageSource;
        this.userContextResolver = userContextResolver;
    }

    public String getMessage(String messageCode, Locale locale) {
        return messageSource.getMessage(messageCode, null, locale);
    }

    public String getCurrentLocaleMessage(String messageCode) {
        return getCurrentLocaleMessage(messageCode, null);
    }

    public String getCurrentLocaleMessage(String messageCode, Object[] args) {
        return messageSource.getMessage(messageCode, args, getCurrentLocale());
    }

    public List<Locale> getSupportedLocales() {
        return List.of(new Locale(RU_LOCALE));
    }

    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    public Locale getCurrentLocale() {
        User user = userContextResolver.getUser();
        for (Locale locale: getSupportedLocales()) {
            if (locale.getLanguage().equals(user.getLanguageCode())) {
                return locale;
            }
        }

        return getDefaultLocale();
    }
}
