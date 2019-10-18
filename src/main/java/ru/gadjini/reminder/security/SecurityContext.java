package ru.gadjini.reminder.security;

import org.telegram.telegrambots.meta.api.objects.User;

public class SecurityContext {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
