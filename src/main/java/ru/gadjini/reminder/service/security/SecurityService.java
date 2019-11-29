package ru.gadjini.reminder.service.security;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.security.SecurityContextHolder;

@Service
public class SecurityService {

    public User getAuthenticatedUser() {
        return SecurityContextHolder.getContext().getUser();
    }

}
