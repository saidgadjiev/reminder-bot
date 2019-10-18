package ru.gadjini.reminder.security;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SecurityContextRepository {

    public SecurityContext loadContext(Update update) {
        SecurityContext securityContext = new SecurityContext();

        if (update.hasMessage()) {
            securityContext.setUser(update.getMessage().getFrom());
        } else if (update.hasCallbackQuery()) {
            securityContext.setUser(update.getCallbackQuery().getFrom());
        }

        return securityContext;
    }
}
