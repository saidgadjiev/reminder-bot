package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommandNavigatorDao {

    private static final String KEY = "command:navigator";

    private StringRedisTemplate redisTemplate;

    @Autowired
    public CommandNavigatorDao(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(long chatId, String command) {
        redisTemplate.opsForHash().put(KEY, String.valueOf(chatId), command);
    }

    public String get(long chatId) {
        return (String) redisTemplate.opsForHash().get(KEY, String.valueOf(chatId));
    }
}
