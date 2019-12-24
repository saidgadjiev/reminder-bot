package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.TgUserDao;
import ru.gadjini.reminder.domain.TgUser;

import java.time.ZoneId;

@Service
public class TgUserService {

    private TgUserDao tgUserDao;

    @Autowired
    public TgUserService(TgUserDao tgUserDao) {
        this.tgUserDao = tgUserDao;
    }

    public TgUser getByUserId(int userId) {
        return tgUserDao.getByUserId(userId);
    }

    public boolean isExists(String username) {
        return tgUserDao.isExists(username);
    }

    public boolean isExists(int userId) {
        return tgUserDao.isExists(userId);
    }

    public TgUser createOrUpdateUser(long chatId, User user) {
        TgUser tgUser = new TgUser();

        tgUser.setUserId(user.getId());
        tgUser.setChatId(chatId);
        tgUser.setUsername(user.getUserName());
        tgUser.setFirstName(user.getFirstName());
        tgUser.setLastName(user.getLastName());

        tgUserDao.createOrUpdate(tgUser);

        return tgUser;
    }

    public ZoneId getTimeZone(int userId) {
        return ZoneId.of("Europe/Moscow");
    }

    public ZoneId getTimeZone(String username) {
        return ZoneId.of("Europe/Moscow");
    }

    public void saveZoneId(int userId, ZoneId zoneId) {
        tgUserDao.updateTimezone(userId, zoneId);
    }
}
