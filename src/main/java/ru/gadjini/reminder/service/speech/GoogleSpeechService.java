package ru.gadjini.reminder.service.speech;

import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Service
public class GoogleSpeechService {

    public String recognize(File file, List<SpeechContext> speechContexts) throws Exception {
        try (SpeechClient speechClient = SpeechClient.create()) {
            // The path to the audio file to transcribe
            // Reads the audio file into memory
            byte[] data = Files.readAllBytes(file.toPath());
            ByteString audioBytes = ByteString.copyFrom(data);

            // Builds the sync recognize request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.OGG_OPUS)
                    .setSampleRateHertz(48000)
                    .setLanguageCode("ru-RU")
                    .setModel("command_and_search")
                    .addAllSpeechContexts(speechContexts)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            if (results.size() > 0) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = results.get(0).getAlternativesList().get(0);

                return alternative.getTranscript();
            }
        }

        return null;
    }
}
