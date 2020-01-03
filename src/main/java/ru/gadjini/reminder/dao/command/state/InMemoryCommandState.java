package ru.gadjini.reminder.dao.command.state;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.configuration.BotConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository

@Profile(BotConfiguration.PROFILE_TEST)
public class InMemoryCommandState implements CommandStateDao {

    private Map<Long, Object> states = new ConcurrentHashMap<>();

    @Override
    public void setState(long chatId, Object state) {
        states.put(chatId, state);
    }

    @Override
    public <T> T getState(long chatId) {
        return (T) states.get(chatId);
    }

    @Override
    public void deleteState(long chatId) {
        states.remove(chatId);
    }
}
