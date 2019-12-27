package ru.gadjini.reminder.service.speech;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FFMpegService {

    private FFmpegExecutor executor;

    public FFMpegService() throws IOException {
        FFmpeg fFmpeg = new FFmpeg("C:\\Program Files\\ffmpeg\\bin\\ffmpeg");
        executor = new FFmpegExecutor(fFmpeg);
    }

    public void execute(FFmpegBuilder job) {
        executor.createJob(job).run();
    }
}
