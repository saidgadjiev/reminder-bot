package ru.gadjini.reminder.service.context;

import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.model.TgMessage;

@Repository
public class UserContextRepository {

    public UserContext loadContext(Update update) {
        return new UserContext(TgMessage.getUser(update));
    }
}
