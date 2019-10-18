package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.TgUserDao;
import ru.gadjini.reminder.domain.TgUser;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TgUserService {

    private TgUserDao tgUserDao;

    @Autowired
    public TgUserService(TgUserDao tgUserDao) {
        this.tgUserDao = tgUserDao;
    }

    public TgUser getUserByUserName(String username) {
        return tgUserDao.getByUserName(username);
    }

    public Map<Integer, TgUser> getUsersByUserIds(Set<Integer> userIds) {
        return tgUserDao.getUsersByUserIds(userIds);
    }

    public void createOrUpdateUser(long chatId, User user) {
        TgUser tgUser = new TgUser();

        tgUser.setUserId(user.getId());
        tgUser.setChatId(chatId);
        tgUser.setUsername(user.getUserName());
        tgUser.setFirstName(user.getFirstName());
        tgUser.setLastName(user.getLastName());

        tgUserDao.createOrUpdate(tgUser);
    }

    public int getUserId(String username) {
        return tgUserDao.getUserId(username);
    }

    public List<TgUser> getAllUsers() {
        return tgUserDao.getAll();
    }
}
