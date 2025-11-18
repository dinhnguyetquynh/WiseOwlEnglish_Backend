package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.CreateVocabReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.service.VocabServiceAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vocab-admin")
@RequiredArgsConstructor
public class VocabAminController {
    private final VocabServiceAdmin vocabServiceAdmin;

    @PostMapping("/create")
    public ResponseEntity<VocabRes> createVocab(@RequestBody CreateVocabReq req){
        VocabRes res = vocabServiceAdmin.createVocab(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
