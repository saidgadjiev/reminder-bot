package ru.gadjini.reminder.dao.command.navigator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.configuration.BotConfiguration;

@Repository
@Profile(BotConfiguration.PROFILE_PROD)
public class RedisCommandNavigatorDao implements CommandNavigatorDao {

    private static final String KEY = "command:navigator";

    private StringRedisTemplate redisTemplate;

    @Autowired
    public RedisCommandNavigatorDao(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void set(long chatId, String command) {
        redisTemplate.opsForHash().put(KEY, String.valueOf(chatId), command);
    }

    @Override
    public String get(long chatId) {
        return (String) redisTemplate.opsForHash().get(KEY, String.valueOf(chatId));
    }
}
