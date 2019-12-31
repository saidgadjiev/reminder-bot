/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.gadjini.reminder.samples;

// [START speech_quickstart]
// Imports the Google Cloud client library

import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import org.apache.commons.lang3.time.StopWatch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class GoogleSpeechToTextSample {

    /**
     * Demonstrates using the Speech API to transcribe an audio file.
     */
    public static void main(String[] args) throws Exception {
        // Instantiates a client
        try (SpeechClient speechClient = SpeechClient.create()) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // The path to the audio file to transcribe
            String fileName = "C:/audio.ogg";

            // Reads the audio file into memory
            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            ByteString audioBytes = ByteString.copyFrom(data);

            // Builds the sync recognize request
            SpeechContext speechContextsElement =
                    SpeechContext.newBuilder().addAllPhrases(List.of("каждые $OOV_CLASS_DIGIT_SEQUENCE часа $OOV_CLASS_DIGIT_SEQUENCE минут")).setBoost(5.0f).build();

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.OGG_OPUS)
                    .setSampleRateHertz(48000)
                    .setLanguageCode("ru-RU")
                    .setModel("command_and_search")
                    .addSpeechContexts(speechContextsElement)
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());
            }
            stopWatch.stop();
            System.out.println(stopWatch.getTime());
        }
    }
}
// [END speech_quickstart]
