package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.FriendshipDao;
import ru.gadjini.reminder.domain.TgUser;

import java.util.List;

@Service
public class FriendshipService {

    private FriendshipDao friendshipDao;

    @Autowired
    public FriendshipService(FriendshipDao friendshipDao) {
        this.friendshipDao = friendshipDao;
    }

    public List<TgUser> getFriendRequests(String username) {
        return friendshipDao.getFriendRequests(username);
    }

    public List<TgUser> getFriends(String username) {
        return null;
    }
}
