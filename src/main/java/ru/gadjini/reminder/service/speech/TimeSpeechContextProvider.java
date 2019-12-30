package ru.gadjini.reminder.service.speech;

import com.google.cloud.speech.v1p1beta1.SpeechContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimeSpeechContextProvider implements SpeechContextProvider {

    private static final float BOOST = 4.0f;

    @Override
    public List<SpeechContext> provide() {
        SpeechContext speechContext = SpeechContext.newBuilder().addAllPhrases(List.of("$MONTH", "$TIME", "$DAY")).setBoost(BOOST).build();
        return List.of(speechContext);
    }
}
