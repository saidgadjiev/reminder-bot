package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.FriendshipDao;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.CreateFriendRequestResult;

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

    public CreateFriendRequestResult createFriendRequest(String friendUsername) {
        User user = securityService.getAuthenticatedUser();

        CreateFriendRequestResult createFriendRequestResult = friendshipDao.createFriendRequest(user.getId(), friendUsername, null, Friendship.Status.REQUESTED);

        setCreateFriendRequestState(user, createFriendRequestResult);

        return createFriendRequestResult;
    }

    public CreateFriendRequestResult createFriendRequest(Integer friendUserId) {
        User user = securityService.getAuthenticatedUser();

        CreateFriendRequestResult createFriendRequestResult = friendshipDao.createFriendRequest(user.getId(), null, friendUserId, Friendship.Status.REQUESTED);

        setCreateFriendRequestState(user, createFriendRequestResult);

        return createFriendRequestResult;
    }

    public List<TgUser> getFriendRequests() {
        User user = securityService.getAuthenticatedUser();

        return friendshipDao.getFriendRequests(user.getId(), Friendship.Status.REQUESTED);
    }

    public List<TgUser> getFriends() {
        User user = securityService.getAuthenticatedUser();

        return friendshipDao.getFriends(user.getId(), Friendship.Status.ACCEPTED);
    }

    public Friendship acceptFriendRequest(int friendId) {
        User user = securityService.getAuthenticatedUser();

        return friendshipDao.acceptFriendRequest(user.getId(), friendId, Friendship.Status.ACCEPTED);
    }

    public Friendship rejectFriendRequest(int friendId) {
        User user = securityService.getAuthenticatedUser();

        return friendshipDao.rejectFriendRequest(user.getId(), friendId);
    }

    private void setCreateFriendRequestState(User currUser, CreateFriendRequestResult createFriendRequestResult) {
        if (createFriendRequestResult.isConflict()) {
            if (createFriendRequestResult.getFriendship().getStatus() == Friendship.Status.REQUESTED) {
                if (createFriendRequestResult.getFriendship().getUserOneId() == currUser.getId()) {
                    createFriendRequestResult.setState(CreateFriendRequestResult.State.ALREADY_REQUESTED);
                } else {
                    createFriendRequestResult.setState(CreateFriendRequestResult.State.ALREADY_REQUESTED_TO_ME);
                }
            } else {
                createFriendRequestResult.setState(CreateFriendRequestResult.State.ALREADY_FRIEND);
            }
        }
    }
}
