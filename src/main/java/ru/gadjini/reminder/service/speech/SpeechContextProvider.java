package ru.gadjini.reminder.service.speech;

import com.google.cloud.speech.v1p1beta1.SpeechContext;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

public interface SpeechContextProvider {

    List<SpeechContext> provide(User user);
}
