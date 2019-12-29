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

package ru.gadjini.reminder;

// [START speech_quickstart]
// Imports the Google Cloud client library

import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.protobuf.ByteString;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class QuickstartSample {

  /**
   * Demonstrates using the Speech API to transcribe an audio file.
   */
  @Test
  void speechTest() throws Exception {
    // Instantiates a client
    try (SpeechClient speechClient = SpeechClient.create()) {
      FFmpeg fFmpeg = new FFmpeg("C:\\Program Files\\ffmpeg\\bin\\ffmpeg");
      FFmpegBuilder builder = new FFmpegBuilder()
              .addInput("C:/audio_2019-12-27_13-19-51.ogg")
              .overrideOutputFiles(true)
              .addOutput("C:\\Program Files\\ffmpeg\\bin\\test.wav")
              .setAudioSampleRate(16000)
              .done();
      FFmpegExecutor executor = new FFmpegExecutor(fFmpeg);

      executor.createJob(builder).run();
      // The path to the audio file to transcribe
      String fileName = "C:\\Program Files\\ffmpeg\\bin\\test.wav";

      // Reads the audio file into memory
      Path path = Paths.get(fileName);
      byte[] data = Files.readAllBytes(path);
      ByteString audioBytes = ByteString.copyFrom(data);

      // Builds the sync recognize request
      RecognitionConfig config = RecognitionConfig.newBuilder()
          .setEncoding(AudioEncoding.LINEAR16)
          .setSampleRateHertz(16000)
          .setLanguageCode("ru-RU")
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
    }
  }
}
// [END speech_quickstart]
