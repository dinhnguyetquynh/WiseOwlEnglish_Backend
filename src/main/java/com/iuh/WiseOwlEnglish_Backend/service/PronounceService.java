package com.iuh.WiseOwlEnglish_Backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.PronounceGradeResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class PronounceService {

    private final RestTemplate restTemplate;
    @Value("${pronunciation.scorer.url}")
    private String scorerUrl;

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
    // ByteArrayResource with filename (ensures Content-Disposition includes filename)
    private ByteArrayResource asResource(File file) throws Exception {
        byte[] data = Files.readAllBytes(file.toPath());
        return new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return file.getName();
            }
        };
    }

    public PronounceGradeResponse score(MultipartFile audioUser, MultipartFile audioRef) throws Exception {
        File userRaw = null, refRaw = null, userFile = null, refFile = null;
        try {
            // 1. save uploaded multipart to temp files
            userRaw = saveTempFile(audioUser, ".u");
            refRaw  = saveTempFile(audioRef, ".r");

            // 2. convert to wav 16k mono
            userFile = convertToWav(userRaw);
            refFile  = convertToWav(refRaw);

            // 3. prepare multipart body to send to FastAPI
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("audioUser", asResource(userFile));
            body.add("audioRef", asResource(refFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(body, headers);

            // 4. call scorer service
            ResponseEntity<JsonNode> resp = restTemplate.postForEntity(scorerUrl, req, JsonNode.class);

            if (resp == null || resp.getBody() == null) {
                throw new RuntimeException("Empty response from scorer service");
            }

            JsonNode bodyNode = resp.getBody();

            PronounceGradeResponse res = new PronounceGradeResponse();
            // Map server response to your DTO (adjust if JSON keys different)
            res.setScore(bodyNode.get("score").asInt());
            // convert grade to expected english codes if necessary (server might return VN strings).
            String gradeStr = bodyNode.has("grade") ? bodyNode.get("grade").asText() : "";
            res.setGrade(bodyNode.get("grade").asText());
            res.setFeedback("Similarity=" + (bodyNode.has("similarity") ? bodyNode.get("similarity").asDouble() : 0.0));
            return res;
        } finally {
            // cleanup temp files
            for (File f : new File[]{userRaw, refRaw, userFile, refFile}) {
                try {
                    if (f != null && f.exists()) f.delete();
                } catch (Exception ignored) {}
            }
        }
    }




}
