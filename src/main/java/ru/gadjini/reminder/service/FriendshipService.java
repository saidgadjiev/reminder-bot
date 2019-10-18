package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.FriendshipDao;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.SendFriendRequestRequest;

import java.util.List;

@Service
public class FriendshipService {

    private FriendshipDao friendshipDao;

    @Autowired
    public FriendshipService(FriendshipDao friendshipDao) {
        this.friendshipDao = friendshipDao;
    }

    public void deleteFriend(int userOneUserId, int userTwoUserId) {
        friendshipDao.deleteFriendShip();
    }

    public void createFriendRequest(SendFriendRequestRequest sendFriendRequestRequest) {
        friendshipDao.createFriendship(sendFriendRequestRequest.getInitiatorUserId(), sendFriendRequestRequest.getReceiverUsername(), Friendship.Status.REQUESTED);
    }

    public List<TgUser> getFriendRequests(int userId) {
        return friendshipDao.getFriendRequests(userId);
    }

    public List<TgUser> getFriends(int currentUserId) {
        return friendshipDao.getFriends(currentUserId);
    }

    public void acceptFriendRequest(int currentUserId, int senderUserId) {
        friendshipDao.updateFriendshipStatus(currentUserId, senderUserId, Friendship.Status.ACCEPTED);
    }

    public void rejectFriendRequest(int currentUserId, int senderUserId) {
        friendshipDao.deleteFriendShip(currentUserId, senderUserId);
    }
}
