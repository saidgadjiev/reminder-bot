package ru.gadjini.reminder.dao.command.navigator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("redis")
public class RedisCommandNavigatorDao implements CommandNavigatorDao {

    private static final String CURRENT_KEY = "command:navigator:current";

    private static final String HISTORY_KEY = "command:navigator:history";

    private StringRedisTemplate redisTemplate;

    @Autowired
    public RedisCommandNavigatorDao(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void set(long chatId, String command) {
        redisTemplate.opsForHash().put(CURRENT_KEY, String.valueOf(chatId), command);
    }

    @Override
    public String get(long chatId) {
        return (String) redisTemplate.opsForHash().get(CURRENT_KEY, String.valueOf(chatId));
    }

    @Override
    public void setParent(long chatId, String command) {
        redisTemplate.opsForList().rightPush(HISTORY_KEY + ":" + chatId, command);
    }

    @Override
    public String getParent(long chatId, String defaultCommand) {
        String result = redisTemplate.opsForList().rightPop(HISTORY_KEY + ":" + chatId);

        return result == null ? defaultCommand : result;
    }
}
