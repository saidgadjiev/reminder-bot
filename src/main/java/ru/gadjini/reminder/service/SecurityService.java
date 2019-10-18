package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.security.SecurityContextHolder;

@Service
public class SecurityService {

    public User getAuthenticatedUser() {
        return SecurityContextHolder.getAuthenticatedUser();
    }

    public void login(User user) {
        SecurityContextHolder.setAuthenticatedUser(user);
    }

    public void logout() {
        SecurityContextHolder.removeAuthenticatedUser();
    }
}
