package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.PronounceGradeResponse;
import com.iuh.WiseOwlEnglish_Backend.exception.ApiException;
import com.iuh.WiseOwlEnglish_Backend.exception.ErrorCode;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class PronounceService {

    private final RestTemplate restTemplate;

    @Value("${assemblyai.api.key}")
    private String apiKey;

    private static final String UPLOAD_URL = "https://api.assemblyai.com/v2/upload";
    private static final String TRANSCRIPT_URL = "https://api.assemblyai.com/v2/transcript";

    public PronounceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PronounceGradeResponse scorePronunciation(MultipartFile audioFile, String targetWord) {
        try {
            // 1. Upload Audio lên AssemblyAI
            String audioUrl = uploadFileToAssemblyAI(audioFile);

            // 2. Yêu cầu Transcribe (Chuyển âm thanh thành văn bản)
            String transcriptId = requestTranscription(audioUrl);

            // 3. Polling để lấy kết quả (Vì AI cần vài giây để xử lý)
            String userText = pollForTranscriptionResult(transcriptId);

            // 4. So sánh và chấm điểm
            return calculateScore(targetWord, userText);

        } catch (Exception e) {
            e.printStackTrace();
            // Trả về lỗi chung nếu có sự cố
            throw new RuntimeException("Lỗi xử lý phát âm: " + e.getMessage());
        }
    }

    // --- Các hàm phụ trợ ---

    private String uploadFileToAssemblyAI(MultipartFile file) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<Map> response = restTemplate.exchange(UPLOAD_URL, HttpMethod.POST, requestEntity, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("upload_url")) {
            throw new RuntimeException("Upload failed");
        }
        return (String) response.getBody().get("upload_url");
    }

    private String requestTranscription(String audioUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("audio_url", audioUrl);
        // Tắt tính năng thêm dấu câu/format để so sánh raw text dễ hơn
        body.put("punctuate", false);
        body.put("format_text", false);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(TRANSCRIPT_URL, requestEntity, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("id")) {
            throw new RuntimeException("Transcription request failed");
        }
        return (String) response.getBody().get("id");
    }

    private String pollForTranscriptionResult(String transcriptId) throws InterruptedException {
        String pollingUrl = TRANSCRIPT_URL + "/" + transcriptId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        while (true) {
            ResponseEntity<Map> response = restTemplate.exchange(pollingUrl, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) throw new RuntimeException("Empty response from polling");

            String status = (String) body.get("status");
            if ("completed".equals(status)) {
                // Trả về text, nếu null (người dùng không nói gì) thì trả về chuỗi rỗng
                return (String) body.getOrDefault("text", "");
            } else if ("error".equals(status)) {
                throw new RuntimeException("Transcription failed: " + body.get("error"));
            }

            // Đợi 1 giây trước khi hỏi lại
            Thread.sleep(1000);
        }
    }

    private PronounceGradeResponse calculateScore(String targetWord, String userText) {
        // Chuẩn hóa chuỗi (chữ thường, bỏ khoảng trắng thừa)
        String s1 = targetWord.trim().toLowerCase();
        String s2 = (userText == null) ? "" : userText.trim().toLowerCase();

        // Loại bỏ dấu câu nếu còn sót
        s2 = s2.replaceAll("[^a-zA-Z0-9 ]", "");

        System.out.println("Target: " + s1 + " | User: " + s2);

        // Trường hợp người dùng không nói gì
        if (s2.isEmpty()) {
            return new PronounceGradeResponse("INACCURATE", 0, "Không nghe thấy giọng nói");
        }

        // Tính khoảng cách Levenshtein
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(s1, s2);

        // Tính điểm % tương đồng
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) maxLength = 1; // tránh chia cho 0

        double similarity = (1.0 - ((double) distance / maxLength)) * 100;
        int score = (int) similarity;

        // Logic xếp loại (Logic tương đối như bạn muốn)
        String grade;
        String feedback;

        if (score >= 90) {
            grade = "ACCURATE"; // Tuyệt vời
            feedback = "Tuyệt vời! (" + s2 + ")";
        } else if (score >= 50) {
            // Ví dụ: red (3) vs re (2) -> distance=1 -> score ~ 66% -> ALMOST
            grade = "ALMOST";   // Gần đúng
            feedback = "Gần đúng rồi (" + s2 + ")";
        } else {
            grade = "INACCURATE"; // Sai
            feedback = "Chưa chính xác (" + s2 + ")";
        }

        return new PronounceGradeResponse(grade, score, feedback);
    }
}