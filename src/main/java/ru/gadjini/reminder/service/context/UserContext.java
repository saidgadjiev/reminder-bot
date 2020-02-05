package ru.gadjini.reminder.service.context;

import org.telegram.telegrambots.meta.api.objects.User;

public class UserContext {

    private final User user;

    public UserContext(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
