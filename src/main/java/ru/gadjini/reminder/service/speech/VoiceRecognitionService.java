package ru.gadjini.reminder.service.speech;

import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gadjini.reminder.service.TelegramService;

import java.io.File;

@Service
public class VoiceRecognitionService {

    private GoogleSpeechService googleSpeechService;

    private TelegramService telegramService;

    private FFMpegService ffMpegService;

    @Autowired
    public VoiceRecognitionService(GoogleSpeechService googleSpeechService, TelegramService telegramService, FFMpegService ffMpegService) {
        this.googleSpeechService = googleSpeechService;
        this.telegramService = telegramService;
        this.ffMpegService = ffMpegService;
    }

    public String recognize(Voice voice) {
        File file = downloadFile(voice.getFileId());
        File out = null;

        try {
            out = File.createTempFile("speech.", ".wav");
            ffMpegService.execute(voiceToRecognizableFormatConfig(file, out));
            return googleSpeechService.recognize(out);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            FileUtils.deleteQuietly(file);
            FileUtils.deleteQuietly(out);
        }
    }

    private FFmpegBuilder voiceToRecognizableFormatConfig(File in, File out) {
        return new FFmpegBuilder()
                .addInput(in.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(out.getAbsolutePath())
                .setAudioSampleRate(16000)
                .done();
    }

    private File downloadFile(String fileId) {
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            org.telegram.telegrambots.meta.api.objects.File file = telegramService.execute(getFile);
            return telegramService.downloadFile(file);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
