package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.PronounceGradeResponse;
import com.iuh.WiseOwlEnglish_Backend.service.PronounceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pronounce")
@RequiredArgsConstructor
public class PronounceController {

    private static final Logger log = LoggerFactory.getLogger(PronounceController.class);
    private final PronounceService pronounceService;

    @PostMapping(
            path = "/score",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PronounceGradeResponse> score(
            @RequestPart("audioUser") MultipartFile audioUser,
            @RequestParam("targetText") String targetText // <-- Thay đổi ở đây
    ) {
        // Gọi service xử lý
        PronounceGradeResponse result = pronounceService.scorePronunciation(audioUser, targetText);
        return ResponseEntity.ok(result);
    }
}
