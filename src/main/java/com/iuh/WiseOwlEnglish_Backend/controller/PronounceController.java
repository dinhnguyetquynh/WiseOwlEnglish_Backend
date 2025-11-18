package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.PronounceGradeResponse;
import com.iuh.WiseOwlEnglish_Backend.service.PronounceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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


    private final PronounceService pronounceService;

    @PostMapping(value = "/grade", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PronounceGradeResponse> grade(
            @RequestPart("audio") MultipartFile audio,
            @RequestPart("correctText") String correctText) throws Exception {

        PronounceGradeResponse resp = pronounceService.gradeAudio(audio, correctText);
        return ResponseEntity.ok(resp);
    }
}
