package ru.gadjini.reminder.model;

import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

import java.util.List;
import java.util.Set;

public class CreateChallengeRequest {

    private List<TgUser> friends;

    private Time challengeTime;

    private Set<Integer> participants;

    private ReminderRequest reminderRequest;

    public List<TgUser> friends() {
        return this.friends;
    }

    public Time challengeTime() {
        return this.challengeTime;
    }

    public Set<Integer> participants() {
        return this.participants;
    }

    public ReminderRequest reminderRequest() {
        return this.reminderRequest;
    }

    public CreateChallengeRequest friends(final List<TgUser> friends) {
        this.friends = friends;
        return this;
    }

    public CreateChallengeRequest challengeTime(final Time challengeTime) {
        this.challengeTime = challengeTime;
        return this;
    }

    public CreateChallengeRequest participants(final Set<Integer> participants) {
        this.participants = participants;
        return this;
    }

    public CreateChallengeRequest reminderRequest(final ReminderRequest reminderRequest) {
        this.reminderRequest = reminderRequest;
        return this;
    }


}
