package com.iuh.WiseOwlEnglish_Backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Service
public class WhisperTranscribeService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String transcriberUrl;

    public WhisperTranscribeService(@Value("${transcriber.url}") String transcriberUrl) {
        this.transcriberUrl = transcriberUrl;
    }

    public String transcribeFile(File file) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(transcriberUrl, requestEntity, JsonNode.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().path("text").asText("");
        } else {
            throw new Exception("Transcriber returned status: " + response.getStatusCode());
        }
    }
}
