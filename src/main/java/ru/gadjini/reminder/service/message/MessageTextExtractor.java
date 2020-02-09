package ru.gadjini.reminder.service.message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.speech.GoogleVoiceRecognitionService;

import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Service
public class MessageTextExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTextExtractor.class);

    private GoogleVoiceRecognitionService voiceRecognitionService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private TgUserService userService;

    private TaskExecutor taskExecutor;

    @Autowired
    public MessageTextExtractor(GoogleVoiceRecognitionService voiceRecognitionService, MessageService messageService,
                                LocalisationService localisationService, TgUserService userService, TaskExecutor taskExecutor) {
        this.voiceRecognitionService = voiceRecognitionService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.taskExecutor = taskExecutor;
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
            taskExecutor.execute(() -> {
                String voiceText = voiceRecognitionService.recognize(message.getFrom(), message.getVoice());

                if (StringUtils.isBlank(voiceText)) {
                    Locale locale = userService.getLocale(message.getFrom().getId());
                    messageService.sendMessageAsync(
                            new SendMessageContext(PriorityJob.Priority.HIGH)
                                    .chatId(message.getChatId())
                                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_VOICE_NOT_RECOGNIZED, locale))
                    );
                    LOGGER.debug("Voice not recognized");
                } else {
                    callback.accept(voiceText.trim());
                }
            });
        }
    }
}
