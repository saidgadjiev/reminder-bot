package ru.gadjini.reminder.domain;

import ru.gadjini.reminder.time.DateTime;

import java.util.List;

public class Challenge {

    public static final String ID = "id";

    public static final String TYPE = "challenge";

    public static final String CREATOR_ID = "creator_id";

    public static final String FINISHED_AT = "finished_at";

    public static final String NAME = "name";

    private int id;

    private String name;

    private int creatorId;

    private TgUser creator;

    private DateTime finishedAt;

    private List<ChallengeParticipant> challengeParticipants;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public TgUser getCreator() {
        return creator;
    }

    public void setCreator(TgUser creator) {
        this.creator = creator;
    }

    public DateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(DateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public List<ChallengeParticipant> getChallengeParticipants() {
        return challengeParticipants;
    }

    public void setChallengeParticipants(List<ChallengeParticipant> challengeParticipants) {
        this.challengeParticipants = challengeParticipants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
