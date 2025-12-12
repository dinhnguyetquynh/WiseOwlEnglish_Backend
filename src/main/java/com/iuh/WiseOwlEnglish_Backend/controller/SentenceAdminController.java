package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateSentenceReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.SentenceUpdateReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.VocabUpdateReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.CreateVocabReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.SentenceAdminRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.SentenceUpdateRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.service.SentenceAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sentences")
@RequiredArgsConstructor
public class SentenceAdminController {
    private final SentenceAdminService adminService;

    @PostMapping("/create")
    public ResponseEntity<SentenceAdminRes> createSentence(@RequestBody CreateSentenceReq req){
        SentenceAdminRes res = adminService.createSentence(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteSentence(@PathVariable Long id) {
        String message = adminService.deleteSentence(id);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SentenceUpdateRes> updateSentence(
            @PathVariable Long id,
            @RequestBody SentenceUpdateReq request // @Valid sẽ kích hoạt kiểm tra lỗi
    ) {
        SentenceUpdateRes res = adminService.updateSentence(id,request);
        return ResponseEntity.ok(res);
    }
}
