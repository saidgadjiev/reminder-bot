package ru.gadjini.reminder.service.speech;

import com.google.cloud.speech.v1p1beta1.SpeechContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class TimeSpeechContextProvider implements SpeechContextProvider {

    private static final float BOOST = 4.0f;

    private List<String> offsetTimePhrases = new ArrayList<>();

    private List<String> repeatTimePhrases = new ArrayList<>();

    private List<String> fixedTimePhrases = new ArrayList<>();

    private DayOfWeekService dayOfWeekService;

    @Autowired
    public TimeSpeechContextProvider(LocalisationService localisationService, DayOfWeekService dayOfWeekService) {
        for (Locale locale: localisationService.getSupportedLocales()) {
            this.offsetTimePhrases.add(localisationService.getMessage(MessagesProperties.OFFSET_TIME_PHRASES, locale));
            this.repeatTimePhrases.add(localisationService.getMessage(MessagesProperties.REPEAT_TIME_PHRASES, locale));
            this.fixedTimePhrases.add(localisationService.getMessage(MessagesProperties.FIXED_TIME_PHRASES, locale));
        }
        this.dayOfWeekService = dayOfWeekService;
    }

    @Override
    public List<SpeechContext> provide(User user) {
        List<String> values = new ArrayList<>();

        for (String phrases: offsetTimePhrases) {
            values.addAll(Arrays.asList(phrases.split(" ")));
        }
        for (String phrases: repeatTimePhrases) {
            values.addAll(Arrays.asList(phrases.split(" ")));
        }
        for (String phrases: fixedTimePhrases) {
            values.addAll(Arrays.asList(phrases.split(" ")));
        }
        values.addAll(Arrays.asList("$MONTH", "$TIME", "$DAY", "$OOV_CLASS_DIGIT_SEQUENCE"));
        values.addAll(dayOfWeekService.getDayOfWeekSpeechPhrases());

        SpeechContext speechContext = SpeechContext.newBuilder().addAllPhrases(values).setBoost(BOOST).build();
        return List.of(speechContext);
    }
}
