package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.LessonProgressReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.VocabUpdateReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.VocabularyDTORes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.service.ProgressTrackingService;
import com.iuh.WiseOwlEnglish_Backend.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {
    private final VocabularyService vocabularyService;
    private final ProgressTrackingService progressTrackingService;

    @GetMapping("/{lessonId}")
    public ResponseEntity<List<VocabularyDTORes>> getVocabulariesByLesson(@PathVariable Long lessonId) {
        var result = vocabularyService.getByLessonId(lessonId);
        return ResponseEntity.ok(result);
    }




}
