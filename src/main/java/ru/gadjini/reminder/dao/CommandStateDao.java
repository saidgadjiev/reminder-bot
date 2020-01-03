package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommandStateDao {

    private static final String KEY = "command:state";

    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CommandStateDao(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setState(long chatId, Object state) {
        redisTemplate.opsForHash().put(KEY, String.valueOf(chatId), state);
    }

    public <T> T getState(long chatId) {
        return (T) redisTemplate.opsForHash().get(KEY, String.valueOf(chatId));
    }

    public void deleteState(long chatId) {
        redisTemplate.opsForHash().delete(KEY, String.valueOf(chatId));
    }
}
