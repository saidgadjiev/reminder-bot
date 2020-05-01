package ru.gadjini.reminder.service.friendship;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.FriendshipDao;
import ru.gadjini.reminder.domain.FriendSearchResult;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.jooq.FriendshipTable;
import ru.gadjini.reminder.model.CreateFriendRequestResult;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.validation.ValidatorFactory;
import ru.gadjini.reminder.service.validation.ValidatorType;
import ru.gadjini.reminder.service.validation.context.UserValidationContext;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class FriendshipService {

    private FriendshipDao friendshipDao;

    private ReminderService reminderService;

    private TgUserService userService;

    private ValidatorFactory validatorFactory;

    @Autowired
    public FriendshipService(FriendshipDao friendshipDao, TgUserService userService) {
        this.friendshipDao = friendshipDao;
        this.userService = userService;
    }

    @Autowired
    public void setValidatorFactory(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Autowired
    public void setReminderService(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Transactional
    public DeleteFriendResult deleteFriend(TgMessage tgMessage, int friendId) {
        Friendship friendship = friendshipDao.deleteFriendship(tgMessage.getUser().getId(), friendId);
        List<Reminder> reminders = reminderService.deleteFriendReminders(tgMessage.getUser().getId(), friendId);

        return new DeleteFriendResult(friendship, reminders);
    }

    @Transactional
    public CreateFriendRequestResult createFriendRequest(TgMessage tgMessage, Integer friendUserId, String friendUsername) {
        User user = tgMessage.getUser();
        Friendship friendship;

        Locale locale = userService.getLocale(tgMessage.getUser().getId());
        if (StringUtils.isNotBlank(friendUsername)) {
            validatorFactory.getValidator(ValidatorType.USER_EXIST).validate(new UserValidationContext().username(friendUsername).locale(locale));
            friendship = getFriendship(user.getId(), friendUsername);
        } else {
            validatorFactory.getValidator(ValidatorType.USER_EXIST).validate(new UserValidationContext().userId(friendUserId).locale(locale));
            friendship = getFriendship(user.getId(), friendUserId);
        }

        if (friendship != null) {
            CreateFriendRequestResult createFriendRequestResult = new CreateFriendRequestResult();

            createFriendRequestResult.setConflict(true);
            createFriendRequestResult.setFriendship(friendship);
            setCreateFriendRequestState(user.getId(), createFriendRequestResult);

            return createFriendRequestResult;
        }

        friendship = new Friendship();

        friendship.setUserOneId(user.getId());
        friendship.setUserOne(TgUser.from(user));

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

        friendship = friendshipDao.createFriendRequest(friendship);

        CreateFriendRequestResult createFriendRequestResult = new CreateFriendRequestResult();

        createFriendRequestResult.setFriendship(friendship);

        setCreateFriendRequestState(user.getId(), createFriendRequestResult);

        return createFriendRequestResult;
    }

    public Set<String> getAllFriendsNames(int userId) {
        return friendshipDao.getAllFriendsNames(userId);
    }

    public TgUser changeFriendName(int userId, int friendId, String name) {
        return friendshipDao.updateFriendName(userId, friendId, Friendship.Status.ACCEPTED, name);
    }

    public TgUser getFriend(int userId, int friendId) {
        return friendshipDao.getFriend(userId, friendId);
    }

    public String getFriendName(int userId, int friendId) {
        return friendshipDao.getFriendName(userId, friendId);
    }

    public FriendSearchResult searchFriend(int userId, Collection<String> nameCandidates) {
        return friendshipDao.searchFriend(userId, nameCandidates);
    }

    public List<TgUser> getToMeFriendRequests(int userId) {
        return friendshipDao.getFriendRequests(
                userId,
                FriendshipTable.TABLE.USER_TWO_ID.eq(userId).and(FriendshipTable.TABLE.STATUS.eq(Friendship.Status.REQUESTED.getCode()))
        );
    }

    public List<TgUser> getFromMeFriendRequests(int userId) {
        return friendshipDao.getFriendRequests(
                userId,
                FriendshipTable.TABLE.USER_ONE_ID.eq(userId).and(FriendshipTable.TABLE.STATUS.eq(Friendship.Status.REQUESTED.getCode()))
        );
    }

    public void cancelFriendRequest(int userId, int friendId) {
        friendshipDao.cancelFriendshipRequest(userId, friendId);
    }

    public List<TgUser> getFriends(int userId) {
        return friendshipDao.getFriends(userId, Friendship.Status.ACCEPTED);
    }

    @Transactional
    public Friendship acceptFriendRequest(User user, int friendId) {
        Friendship friendship = friendshipDao.updateFriendshipStatus(user.getId(), friendId, Friendship.Status.ACCEPTED);

        if (friendship == null) {
            return null;
        }

        friendship.setUserTwo(TgUser.from(user));
        friendship.setUserTwoId(user.getId());

        return friendship;
    }

    @Transactional
    public Friendship rejectFriendRequest(User user, int friendId) {
        Friendship friendship = friendshipDao.updateFriendshipStatus(user.getId(), friendId, Friendship.Status.REJECTED);

        friendship.setUserTwo(TgUser.from(user));
        friendship.setUserTwoId(user.getId());

        return friendship;
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

    private void setCreateFriendRequestState(int userId, CreateFriendRequestResult createFriendRequestResult) {
        if (createFriendRequestResult.isConflict()) {
            if (createFriendRequestResult.getFriendship().getStatus() == Friendship.Status.REQUESTED) {
                if (createFriendRequestResult.getFriendship().getUserOneId() == userId) {
                    createFriendRequestResult.setState(CreateFriendRequestResult.State.ALREADY_REQUESTED);
                } else {
                    createFriendRequestResult.setState(CreateFriendRequestResult.State.ALREADY_REQUESTED_TO_ME);
                }
            } else {
                createFriendRequestResult.setState(CreateFriendRequestResult.State.ALREADY_FRIEND);
            }
        }
    }

    public static class DeleteFriendResult {

        private Friendship friendship;

        private List<Reminder> reminders;

        public DeleteFriendResult(Friendship friendship, List<Reminder> reminders) {
            this.friendship = friendship;
            this.reminders = reminders;
        }

        public Friendship getFriendship() {
            return friendship;
        }

        public List<Reminder> getReminders() {
            return reminders;
        }
    }
}
