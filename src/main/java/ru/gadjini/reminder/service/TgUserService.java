package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.TgUserDao;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.time.ZoneId;

@Service
public class TgUserService {

    private TgUserDao tgUserDao;

    private LocalisationService localisationService;

    @Autowired
    public TgUserService(TgUserDao tgUserDao, LocalisationService localisationService) {
        this.tgUserDao = tgUserDao;
        this.localisationService = localisationService;
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
        String zone = tgUserDao.getTimeZone(userId);

        if (zone == null) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_TIMEZONE_NOT_EXISTS));
        }

        return ZoneId.of(zone);
    }

    public ZoneId getTimeZone(String username) {
        String zone = tgUserDao.getTimeZone(username);

        if (zone == null) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_TIMEZONE_NOT_EXISTS));
        }

        return ZoneId.of(zone);
    }

    public void saveZoneId(int userId, ZoneId zoneId) {
        tgUserDao.updateTimezone(userId, zoneId.getId());
    }
}
