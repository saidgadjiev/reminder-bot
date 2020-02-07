package ru.gadjini.reminder.service.message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.service.speech.GoogleVoiceRecognitionService;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

@Service
public class MessageTextExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTextExtractor.class);

    private GoogleVoiceRecognitionService voiceRecognitionService;

    @Autowired
    public MessageTextExtractor(GoogleVoiceRecognitionService voiceRecognitionService) {
        this.voiceRecognitionService = voiceRecognitionService;
    }

    public void extract(Message message, Consumer<String> callback, Callable<Void> waiting) {
        if (message.hasText()) {
            callback.accept(message.getText().trim());
        }
        if (message.hasVoice()) {
            try {
                waiting.call();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            ForkJoinPool.commonPool().execute(() -> {
                String voiceText = voiceRecognitionService.recognize(message.getFrom(), message.getVoice());

                if (StringUtils.isBlank(voiceText)) {
                    LOGGER.debug("Voice not recognized");
                } else {
                    callback.accept(voiceText);
                }
            });
        }
    }
}
