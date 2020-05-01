package ru.gadjini.reminder.service.validation.context;

import ru.gadjini.reminder.bot.command.keyboard.challenge.ChallengeState;

public class ChallengeStateContext implements ValidationContext {

    private ChallengeState challengeState;

    public ChallengeState challengeState() {
        return this.challengeState;
    }

    public ChallengeStateContext challengeState(final ChallengeState challengeState) {
        this.challengeState = challengeState;
        return this;
    }
}
