package ru.gadjini.reminder.service.speech;

import com.google.cloud.speech.v1p1beta1.SpeechContext;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TimeSpeechContextProvider implements SpeechContextProvider {

    private static final float BOOST = 4.0f;

    private String offsetTimePhrases;

    private String repeatTimePhrases;

    private String fixedTimePhrases;

    public TimeSpeechContextProvider(LocalisationService localisationService) {
        this.offsetTimePhrases = localisationService.getMessage(MessagesProperties.OFFSET_TIME_PHRASES);
        this.repeatTimePhrases = localisationService.getMessage(MessagesProperties.REPEAT_TIME_PHRASES);
        this.fixedTimePhrases = localisationService.getMessage(MessagesProperties.FIXED_TIME_PHRASES);
    }

    @Override
    public List<SpeechContext> provide() {
        List<String> values = new ArrayList<>();

        values.addAll(Arrays.asList(offsetTimePhrases.split(" ")));
        values.addAll(Arrays.asList(repeatTimePhrases.split(" ")));
        values.addAll(Arrays.asList(fixedTimePhrases.split(" ")));
        values.addAll(Arrays.asList("$MONTH", "$TIME", "$DAY", "$OOV_CLASS_DIGIT_SEQUENCE"));

        SpeechContext speechContext = SpeechContext.newBuilder().addAllPhrases(values).setBoost(BOOST).build();
        return List.of(speechContext);
    }
}
