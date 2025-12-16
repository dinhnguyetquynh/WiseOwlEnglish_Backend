package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.LessonLockStatusReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.LessonProgressReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonLockStatusRes;
import com.iuh.WiseOwlEnglish_Backend.service.ProgressTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lesson-progress")
@RequiredArgsConstructor
public class LessonProgressController {
    private final ProgressTrackingService progressTrackingService;

    @PostMapping("/mark-completed")
    @PreAuthorize("hasRole('LEARNER')")
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

    @GetMapping("/lock-status")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<LessonLockStatusRes> getLessonLockStatus(@RequestParam Long lessonId,@RequestParam Long profileId){
        LessonLockStatusRes res = progressTrackingService.getLessonLockStatus(profileId,lessonId);
        return ResponseEntity.ok(res);
    }

}
