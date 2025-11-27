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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<?> score(
            @RequestPart("audioUser") MultipartFile audioUser,
            @RequestPart("audioRef") MultipartFile audioRef
    ) {
        try {
            if (audioUser == null || audioUser.isEmpty()) {
                return ResponseEntity.badRequest().body("audioUser is required");
            }
            if (audioRef == null || audioRef.isEmpty()) {
                return ResponseEntity.badRequest().body("audioRef is required");
            }

            PronounceGradeResponse result = pronounceService.score(audioUser, audioRef);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException ex) {
            log.warn("Invalid request to /api/pronounce/score: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to score pronunciation", ex);
            // 502 Bad Gateway if scorer service or conversion fails externally
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Failed to score pronunciation: " + ex.getMessage());
        }
    }
}
