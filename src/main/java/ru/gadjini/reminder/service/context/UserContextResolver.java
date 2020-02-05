package ru.gadjini.reminder.service.context;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
public class UserContextResolver {

    public User getUser() {
        return UserContextHolder.get().getUser();
    }
}
