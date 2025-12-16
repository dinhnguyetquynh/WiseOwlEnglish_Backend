package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.VocabUpdateReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.CreateVocabReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.service.VocabServiceAdmin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vocab")
@RequiredArgsConstructor
public class VocabAminController {
    private final VocabServiceAdmin vocabServiceAdmin;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VocabRes> createVocab(@RequestBody CreateVocabReq req){
        VocabRes res = vocabServiceAdmin.createVocab(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteVocab(@PathVariable Long id) {
        String message = vocabServiceAdmin.deleteVocab(id);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VocabRes> updateVocabulary(
            @PathVariable Long id,
            @RequestBody VocabUpdateReq request // @Valid sẽ kích hoạt kiểm tra lỗi
    ) {
        VocabRes response = vocabServiceAdmin.updateVocabulary(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/import/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> importExcel(
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file) {

        // Kiểm tra định dạng file
        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body(List.of("Vui lòng chỉ tải lên file Excel (.xlsx)"));
        }

        List<String> result =  vocabServiceAdmin.importVocabulariesFromExcel(file, lessonId);
        return ResponseEntity.ok(result);
    }

}
