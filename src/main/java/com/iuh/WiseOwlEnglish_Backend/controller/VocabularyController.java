package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.VocabularyDTORes;
import com.iuh.WiseOwlEnglish_Backend.service.VocabularyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vocabularies")
public class VocabularyController {
    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<List<VocabularyDTORes>> getVocabulariesByLesson(@PathVariable Long lessonId) {
        var result = vocabularyService.getByLessonId(lessonId);
        return ResponseEntity.ok(result);
    }
}
