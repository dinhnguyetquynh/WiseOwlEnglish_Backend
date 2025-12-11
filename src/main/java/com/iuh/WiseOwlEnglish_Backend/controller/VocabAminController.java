package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.VocabUpdateReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.CreateVocabReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.service.VocabServiceAdmin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/vocab")
@RequiredArgsConstructor
public class VocabAminController {
    private final VocabServiceAdmin vocabServiceAdmin;

    @PostMapping("/create")
    public ResponseEntity<VocabRes> createVocab(@RequestBody CreateVocabReq req){
        VocabRes res = vocabServiceAdmin.createVocab(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteVocab(@PathVariable Long id) {
        String message = vocabServiceAdmin.deleteVocab(id);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<VocabRes> updateVocabulary(
            @PathVariable Long id,
            @RequestBody VocabUpdateReq request // @Valid sẽ kích hoạt kiểm tra lỗi
    ) {
        VocabRes response = vocabServiceAdmin.updateVocabulary(id, request);
        return ResponseEntity.ok(response);
    }


}
