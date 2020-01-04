package ru.gadjini.reminder.dao.usernotification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.UserReminderNotification;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Qualifier("redis")
@Repository
public class RedisUserReminderNotificationDao implements UserReminderNotificationDao {

    private static final String KEY = "user:reminder:notification";

    private RedisTemplate<String, Object> redisTemplate;

    private UserReminderNotificationDao dbDao;

    @Autowired
    public RedisUserReminderNotificationDao(RedisTemplate<String, Object> redisTemplate,
                                            @Qualifier("db") UserReminderNotificationDao dbDao) {
        this.redisTemplate = redisTemplate;
        this.dbDao = dbDao;
    }

    @Override
    public void deleteById(int id) {
        dbDao.deleteById(id);
    }

    @Override
    public void create(UserReminderNotification userReminderNotification) {
        dbDao.create(userReminderNotification);
    }

    @Override
    public int count(int userId, UserReminderNotification.NotificationType notificationType) {
        return dbDao.count(userId, notificationType);
    }

    @Override
    public List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType) {
        String key = getKey(userId, notificationType);
        Set<Object> members = redisTemplate.opsForSet().members(key);

        if (members == null) {
            List<UserReminderNotification> list = dbDao.getList(userId, notificationType);
            redisTemplate.opsForSet().add(key, list.toArray());
            redisTemplate.expire(key, 1, TimeUnit.DAYS);

            return list;
        } else {
            return members.stream().map(o -> (UserReminderNotification) o).collect(Collectors.toList());
        }
    }

    private String getKey(int userId, UserReminderNotification.NotificationType notificationType) {
        return KEY + ":" + userId + ":" + notificationType.name();
    }
}
