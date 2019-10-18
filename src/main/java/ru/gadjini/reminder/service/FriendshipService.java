package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.FriendshipDao;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;

import java.util.List;

@Service
public class FriendshipService {

    private FriendshipDao friendshipDao;

    private SecurityService securityService;

    @Autowired
    public FriendshipService(FriendshipDao friendshipDao, SecurityService securityService) {
        this.friendshipDao = friendshipDao;
        this.securityService = securityService;
    }

    public void deleteFriend(int friendId) {
        User user = securityService.getAuthenticatedUser();

        friendshipDao.deleteFriend(user.getId(), friendId);
    }

    public void createFriendRequest(String friendUsername) {
        User user = securityService.getAuthenticatedUser();

        friendshipDao.createFriendship(user.getId(), friendUsername, Friendship.Status.REQUESTED);
    }

    public List<TgUser> getFriendRequests() {
        User user = securityService.getAuthenticatedUser();

        return friendshipDao.getFriendRequests(user.getId());
    }

    public List<TgUser> getFriends() {
        User user = securityService.getAuthenticatedUser();

        return friendshipDao.getFriends(user.getId());
    }

    public void acceptFriendRequest(int friendId) {
        User user = securityService.getAuthenticatedUser();

        friendshipDao.updateFriendshipStatus(user.getId(), friendId, Friendship.Status.ACCEPTED);
    }

    public void rejectFriendRequest(int friendId) {
        User user = securityService.getAuthenticatedUser();

        friendshipDao.deleteFriendRequest(user.getId(), friendId);
    }
}
