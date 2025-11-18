package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.PronounceGradeResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class PronounceService {
    private final WhisperTranscribeService whisperTranscribeService;

    // Save multipart to temp file
    private File saveTempFile(MultipartFile mp, String suffix) throws Exception {
        File tmp = File.createTempFile("upload-", suffix);
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(mp.getBytes());
        }
        return tmp;
    }

    // Convert to wav using ffmpeg. Input can be webm/ogg/mp3/wav. Output WAV 16k mono.
    private File convertToWav(File input) throws Exception {
        String in = input.getAbsolutePath();
        File out = File.createTempFile("conv-", ".wav");
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", in,
                "-ar", "16000",
                "-ac", "1",
                "-vn",
                "-f", "wav",
                out.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        int code = p.waitFor();
        if (code != 0) {
            throw new RuntimeException("ffmpeg convert failed with exit code " + code);
        }
        return out;
    }

    public PronounceGradeResponse gradeAudio(MultipartFile audioFile, String correctText) throws Exception {
        File tmpIn = null;
        File wav = null;
        try {
            tmpIn = saveTempFile(audioFile, ".upload");
            wav = convertToWav(tmpIn);

            // 1) Transcribe with Vosk
            String transcript = whisperTranscribeService.transcribeFile(wav);

            // 2) Score by Levenshtein
            PronounceGradeResponse resp = scoreTranscript(transcript, correctText);

            return resp;
        } finally {
            // cleanup
            if (tmpIn != null) try { Files.deleteIfExists(tmpIn.toPath()); } catch (Exception ignored) {}
            if (wav != null) try { Files.deleteIfExists(wav.toPath()); } catch (Exception ignored) {}
        }
    }

    private PronounceGradeResponse scoreTranscript(String transcript, String correctText) {
        if (transcript == null) transcript = "";
        if (correctText == null) correctText = "";

        String t = transcript.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        String c = correctText.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();

//        if (c.isEmpty()) {
//            return new PronounceGradeResponse("INACCURATE", 0, "No reference text to compare.");
//        }

        LevenshteinDistance ld = new LevenshteinDistance();
        int distance = ld.apply(t, c);
        int maxLen = Math.max(t.length(), c.length());
        int score = maxLen == 0 ? 100 : Math.max(0, (int) Math.round((1.0 - (double) distance / maxLen) * 100));

        String grade;
        if (score >= 85) grade = "CHÍNH XÁC";
        else if (score >= 60) grade = "GẦN ĐÚNG";
        else grade = "CHƯA CHÍNH XÁC RỒI";

        String feedback = String.format("%s.Bạn phát âm gần giống: %d%%", grade, score);

//        return new PronounceGradeResponse(grade, score, feedback);
        PronounceGradeResponse res = new PronounceGradeResponse();
        res.setGrade(grade);
        res.setScore(score);
        res.setFeedback(feedback);
        return res;
    }

}
