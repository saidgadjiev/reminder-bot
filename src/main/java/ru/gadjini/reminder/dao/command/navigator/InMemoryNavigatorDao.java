package ru.gadjini.reminder.dao.command.navigator;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.configuration.BotConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile(BotConfiguration.PROFILE_TEST)
public class InMemoryNavigatorDao implements CommandNavigatorDao {

    private Map<Long, String> commands = new ConcurrentHashMap<>();

    @Override
    public void set(long chatId, String command) {
        commands.put(chatId, command);
    }

    @Override
    public String get(long chatId) {
        return commands.get(chatId);
    }
}
