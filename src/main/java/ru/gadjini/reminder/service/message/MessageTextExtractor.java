package ru.gadjini.reminder.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.service.speech.VoiceRecognitionService;

@Service
public class MessageTextExtractor {

    private VoiceRecognitionService voiceRecognitionService;

    @Autowired
    public MessageTextExtractor(VoiceRecognitionService voiceRecognitionService) {
        this.voiceRecognitionService = voiceRecognitionService;
    }

    public String extract(Message message) {
        if (message.hasText()) {
            return message.getText().trim();
        }
        if (message.hasVoice()) {
            String voiceText = voiceRecognitionService.recognize(message.getVoice());

            return voiceText == null ? null : voiceText.trim();
        }

        return null;
    }
}
