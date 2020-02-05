package ru.gadjini.reminder.service.speech;

import com.google.cloud.speech.v1p1beta1.SpeechContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.*;

@Component
public class FriendsNamesSpeechContextProvider implements SpeechContextProvider {

    private static final float UPPER_BOOST_BOUND = 10.f;

    private static final float LOWER_BOOST_BOUND = 5.f;

    private Set<String> forFriendStarts = new HashSet<>();

    private FriendshipService friendshipService;

    @Autowired
    public FriendsNamesSpeechContextProvider(LocalisationService localisationService, FriendshipService friendshipService) {
        this.friendshipService = friendshipService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.forFriendStarts.add(localisationService.getMessage(MessagesProperties.FOR_FRIEND_REMINDER_START, locale));
        }
    }

    @Override
    public List<SpeechContext> provide(User user) {
        Set<String> friendsNames = friendshipService.getAllFriendsNames(user.getId());
        float step = (UPPER_BOOST_BOUND - LOWER_BOOST_BOUND) / friendsNames.size();

        float startBoost = LOWER_BOOST_BOUND;
        List<SpeechContext> speechContexts = new ArrayList<>();
        for (String forFriendStart: forFriendStarts) {
            for (String name : friendsNames) {
                String phrase = forFriendStart + " " + name;
                SpeechContext speechContext = SpeechContext.newBuilder().addPhrases(phrase).setBoost(startBoost).buildPartial();

                startBoost += step;
                speechContexts.add(speechContext);
            }
        }

        return speechContexts;
    }
}
