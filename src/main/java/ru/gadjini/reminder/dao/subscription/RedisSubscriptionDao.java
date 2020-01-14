package ru.gadjini.reminder.dao.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.Subscription;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Qualifier("redis")
public class RedisSubscriptionDao implements SubscriptionDao {

    private static final String KEY = "subscription";

    private RedisTemplate<String, Object> redisTemplate;

    private StringRedisTemplate stringRedisTemplate;

    private SubscriptionDao subscriptionDao;

    private ObjectMapper objectMapper;

    @Autowired
    public RedisSubscriptionDao(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate, @Qualifier("db") SubscriptionDao subscriptionDao, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.subscriptionDao = subscriptionDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public Subscription getSubscription(int userId) {
        Subscription fromRedis = getFromRedis(userId);

        if (fromRedis != null) {
            return fromRedis;
        }
        Subscription fromDb = subscriptionDao.getSubscription(userId);

        if (fromDb != null) {
            storeToRedis(fromDb);
        }

        return fromDb;
    }

    @Override
    public void create(Subscription subscription) {
        subscriptionDao.create(subscription);
        storeToRedis(subscription);
    }

    @Override
    public LocalDate update(Period period, int planId, int userId) {
        LocalDate result = subscriptionDao.update(period, planId, userId);

        redisTemplate.opsForHash().putAll(getKey(userId), Map.of(Subscription.PLAN_ID, planId, Subscription.END_DATE, result));

        return result;
    }

    private Subscription getFromRedis(int userId) {
        String key = getKey(userId);

        if (redisTemplate.hasKey(key)) {
            List<Object> objects = stringRedisTemplate.opsForHash().multiGet(getKey(userId), List.of(Subscription.PLAN_ID, Subscription.END_DATE));

            try {
                Subscription subscription = new Subscription();

                subscription.setPlanId(objects.get(0) == null ? null : objectMapper.readValue((String) objects.get(0), Integer.class));
                subscription.setEndDate(objectMapper.readValue((String) objects.get(1), LocalDate.class));
                subscription.setUserId(userId);

                return subscription;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return null;
    }

    private void storeToRedis(Subscription subscription) {
        String key = getKey(subscription.getUserId());

        Map<String, Object> values = new HashMap<>();
        if (subscription.getPlanId() != null) {
            values.put(Subscription.PLAN_ID, subscription.getPlanId());
        }
        values.put(Subscription.END_DATE, subscription.getEndDate());

        redisTemplate.opsForHash().putAll(key, values);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    private String getKey(int userId) {
        return KEY + ":" + userId;
    }
}
