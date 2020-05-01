package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.bot.command.keyboard.challenge.ChallengeState;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.ChallengeStateContext;

import java.util.Locale;

@Component
public class ChallengeStateValidator implements Validator<ChallengeStateContext> {

    private LocalisationService localisationService;

    @Autowired
    public ChallengeStateValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.CHALLENGE_STATE;
    }

    @Override
    public void validate(ChallengeStateContext validationContext) {
        if (validationContext.challengeState().getState() == ChallengeState.State.PARTICIPANTS && validationContext.challengeState().getParticipants().isEmpty()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_PARTICIPANTS_REQUIRED, new Locale(validationContext.challengeState().getUserLanguage())));
        }
    }
}
