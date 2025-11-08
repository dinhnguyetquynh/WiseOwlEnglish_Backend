package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.LessonProgressReq;
import com.iuh.WiseOwlEnglish_Backend.service.ProgressTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lesson-progress")
@RequiredArgsConstructor
public class LessonProgressController {
    private final ProgressTrackingService progressTrackingService;

    @PostMapping("/mark-completed")
    public ResponseEntity<Void> markItemCompleted(
            @Valid @RequestBody LessonProgressReq req
    ) {
        progressTrackingService.markItemCompleted(
                req.getLearnerProfileId(),
                req.getLessonId(),
                req.getItemType(),
                req.getItemRefId()
        );
        return ResponseEntity.ok().build();
    }

}
