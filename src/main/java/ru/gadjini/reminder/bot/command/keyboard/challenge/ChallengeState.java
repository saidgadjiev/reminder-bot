package ru.gadjini.reminder.bot.command.keyboard.challenge;

import ru.gadjini.reminder.bot.command.state.ReminderRequestData;
import ru.gadjini.reminder.bot.command.state.TimeData;
import ru.gadjini.reminder.bot.command.state.UserData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChallengeState {

    private State state = State.TEXT;

    private ReminderRequestData reminderRequest;

    private TimeData time;

    private Set<Long> participants = new LinkedHashSet<>();

    private List<UserData> friends = new ArrayList<>();

    private String userLanguage;

    private String challengeName;

    private String duration;

    private int messageId;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public ReminderRequestData getReminderRequest() {
        return reminderRequest;
    }

    public void setReminderRequest(ReminderRequestData reminderRequest) {
        this.reminderRequest = reminderRequest;
    }

    public TimeData getTime() {
        return time;
    }

    public void setTime(TimeData time) {
        this.time = time;
    }

    public Set<Long> getParticipants() {
        return participants;
    }

    public void addOrRemoveParticipant(long id) {
        if (!participants.add(id)) {
            participants.remove(id);
        }
    }

    public List<UserData> getFriends() {
        return friends;
    }

    public void setFriends(List<UserData> friends) {
        this.friends = friends;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public void setUserLanguage(String userLanguage) {
        this.userLanguage = userLanguage;
    }

    public String getChallengeName() {
        return challengeName;
    }

    public void setChallengeName(String challengeName) {
        this.challengeName = challengeName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public enum State {
        TEXT,
        TIME,
        PARTICIPANTS,
        CREATED
    }
}
