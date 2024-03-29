package ru.gadjini.reminder.service.speech;

import com.google.cloud.speech.v1p1beta1.SpeechContext;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Voice;
import ru.gadjini.reminder.service.TelegramService;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoogleVoiceRecognitionService {

    private GoogleSpeechService googleSpeechService;

    private TelegramService telegramService;

    private final Set<SpeechContextProvider> speechContextProviders;

    @Autowired
    public GoogleVoiceRecognitionService(GoogleSpeechService googleSpeechService, TelegramService telegramService, Set<SpeechContextProvider> speechContextProviders) {
        this.googleSpeechService = googleSpeechService;
        this.telegramService = telegramService;
        this.speechContextProviders = speechContextProviders;
    }

    public String recognize(User user, Voice voice) {
        File file = telegramService.downloadFileByFileId(voice.getFileId());

        try {
            List<SpeechContext> speechContexts = speechContextProviders.stream()
                    .map(speechContextProvider -> speechContextProvider.provide(user))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            return googleSpeechService.recognize(file, speechContexts);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }
}
