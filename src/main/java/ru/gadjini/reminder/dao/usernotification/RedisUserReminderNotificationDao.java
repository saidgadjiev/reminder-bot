package ru.gadjini.reminder.dao.usernotification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.UserReminderNotification;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Qualifier("redis")
@Repository
public class RedisUserReminderNotificationDao implements UserReminderNotificationDao {

    private static final String USER_REMINDER_NOTIFICATION_KEY = "user_reminder_notification";

    private RedisTemplate<String, Object> redisTemplate;

    private UserReminderNotificationDao dbDao;

    @Autowired
    public RedisUserReminderNotificationDao(RedisTemplate<String, Object> redisTemplate,
                                            @Qualifier("db") UserReminderNotificationDao dbDao) {
        this.redisTemplate = redisTemplate;
        this.dbDao = dbDao;
    }

    @Override
    public UserReminderNotification deleteById(int id) {
        UserReminderNotification deleted = dbDao.deleteById(id);
        redisTemplate.delete(getKey(deleted.getUserId(), deleted.getType()));

        return deleted;
    }

    @Override
    public void create(UserReminderNotification userReminderNotification) {
        dbDao.create(userReminderNotification);

        redisTemplate.delete(getKey(userReminderNotification.getUserId(), userReminderNotification.getType()));
    }

    @Override
    public int count(int userId, UserReminderNotification.NotificationType notificationType) {
        return dbDao.count(userId, notificationType);
    }

    @Override
    public List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType, boolean useCache) {
        if (useCache) {
            String key = getKey(userId, notificationType);
            List<Object> members = redisTemplate.opsForList().range(key, 0, -1);

            if (members == null || members.isEmpty()) {
                List<UserReminderNotification> list = dbDao.getList(userId, notificationType, true);

                if (!list.isEmpty()) {
                    //TODO: fix on redis version >= 2.4.5. Not working multiple push on windows redis 2.4.5
                    for (UserReminderNotification notification : list) {
                        redisTemplate.opsForList().rightPush(key, notification);
                    }
                    redisTemplate.expire(key, 1, TimeUnit.DAYS);
                }

                return list;
            } else {
                return members.stream().map(o -> (UserReminderNotification) o).collect(Collectors.toList());
            }
        } else {
            return dbDao.getList(userId, notificationType, false);
        }
    }

    private String getKey(int userId, UserReminderNotification.NotificationType notificationType) {
        return USER_REMINDER_NOTIFICATION_KEY + ":" + userId + "_" + notificationType.getCode();
    }
}
