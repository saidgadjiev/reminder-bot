package ru.gadjini.reminder.dao.usernotification;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.UserReminderNotification;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Qualifier("inMemory")
@Repository
public class InMemoryUserReminderNotificationDao implements UserReminderNotificationDao {

    private LoadingCache<String, List<UserReminderNotification>> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Nullable
                @Override
                public List<UserReminderNotification> load(@NonNull String key) {
                    Object[] objects = splitKey(key);

                    return dbDao.getList((int) objects[0], (UserReminderNotification.NotificationType) objects[1], true);
                }
            });

    private UserReminderNotificationDao dbDao;

    @Autowired
    public InMemoryUserReminderNotificationDao(@Qualifier("db") UserReminderNotificationDao dbDao) {
        this.dbDao = dbDao;
    }

    @Override
    public UserReminderNotification deleteById(int id) {
        UserReminderNotification deleted = dbDao.deleteById(id);
        cache.invalidate(getKey(deleted.getUserId(), deleted.getType()));
        return deleted;
    }

    @Override
    public void create(UserReminderNotification userReminderNotification) {
        dbDao.create(userReminderNotification);

        cache.invalidate(getKey(userReminderNotification.getUserId(), userReminderNotification.getType()));
    }

    @Override
    public int count(int userId, UserReminderNotification.NotificationType notificationType) {
        return cache.get(getKey(userId, notificationType)).size();
    }

    @Override
    public List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType, boolean useCache) {
        if (useCache) {
            return cache.get(getKey(userId, notificationType));
        } else {
            return dbDao.getList(userId, notificationType, false);
        }
    }

    private String getKey(int userId, UserReminderNotification.NotificationType notificationType) {
        return userId + ":" + notificationType.name();
    }

    private Object[] splitKey(String key) {
        String[] args = key.split(":");

        return new Object[]{Integer.parseInt(args[0]), UserReminderNotification.NotificationType.valueOf(args[1])};
    }
}
