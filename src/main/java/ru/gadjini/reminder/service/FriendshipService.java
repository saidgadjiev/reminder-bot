package ru.gadjini.reminder.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public CreateFriendRequestResult createFriendRequest(Integer friendUserId, String friendUsername) {
        User user = securityService.getAuthenticatedUser();
        Friendship friendship;

        if (StringUtils.isNotBlank(friendUsername)) {
            friendship = getFriendship(user.getId(), friendUsername);
        } else {
            friendship = getFriendship(user.getId(), friendUserId);
        }

        if (friendship != null) {
            CreateFriendRequestResult createFriendRequestResult = new CreateFriendRequestResult();

            createFriendRequestResult.setConflict(true);
            createFriendRequestResult.setFriendship(friendship);
            setCreateFriendRequestState(user, createFriendRequestResult);

            return createFriendRequestResult;
        }

        friendship = new Friendship();
        friendship.setUserOneId(user.getId());
        if (friendUserId != null) {
            friendship.setUserTwoId(friendUserId);
            TgUser userTwo = new TgUser();
            userTwo.setUserId(friendUserId);
            friendship.setUserTwo(userTwo);
        } else {
            TgUser userTwo = new TgUser();
            userTwo.setUsername(friendUsername);
            friendship.setUserTwo(userTwo);
        }
        friendship.setStatus(Friendship.Status.REQUESTED);

        friendship = friendshipDao.createFriendship(friendship);

        CreateFriendRequestResult createFriendRequestResult = new CreateFriendRequestResult();

        createFriendRequestResult.setFriendship(friendship);

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

    @Transactional
    public Friendship acceptFriendRequest(int friendId) {
        User user = securityService.getAuthenticatedUser();

        return friendshipDao.acceptFriendRequest(user.getId(), friendId, Friendship.Status.ACCEPTED);
    }

    @Transactional
    public Friendship rejectFriendRequest(int friendId) {
        User user = securityService.getAuthenticatedUser();

        return friendshipDao.rejectFriendRequest(user.getId(), friendId);
    }

    public Friendship getFriendship(int userId, int friendId) {
        return friendshipDao.getFriendship(userId, friendId);
    }

    public Friendship getFriendship(int userId, String friendUsername) {
        return friendshipDao.getFriendship(userId, friendUsername);
    }

    public boolean existFriendship(int userId, String friendUsername, Friendship.Status status) {
        Friendship friendship = getFriendship(userId, friendUsername);

        return friendship != null && friendship.getStatus() == status;
    }

    public boolean existFriendship(int userId, int friendId, Friendship.Status status) {
        Friendship friendship = getFriendship(userId, friendId);

        return friendship != null && friendship.getStatus() == status;
    }

    public boolean existFriendship(int friendId, Friendship.Status status) {
        User user = securityService.getAuthenticatedUser();

        return existFriendship(user.getId(), friendId, status);
    }

    public boolean existFriendship(String friendUsername, Friendship.Status status) {
        User user = securityService.getAuthenticatedUser();

        return existFriendship(user.getId(), friendUsername, status);
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
