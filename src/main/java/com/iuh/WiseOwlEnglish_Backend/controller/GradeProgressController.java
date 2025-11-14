package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.GradeProgress;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonProgressDetailRes;
import com.iuh.WiseOwlEnglish_Backend.service.GradeProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grade-progress")
@RequiredArgsConstructor
public class GradeProgressController {
    private final GradeProgressService gradeProgressService;

    @GetMapping("/by-grade")
    public ResponseEntity<GradeProgress> getGradeProgress(
            @RequestParam int orderIndex,
            @RequestParam("profileId") Long profileId
    ) {
        GradeProgress res = gradeProgressService.getGradeProgress(orderIndex, profileId);
        return ResponseEntity.ok(res);
    }
    @GetMapping("/lesson-detail")
    public ResponseEntity<LessonProgressDetailRes> getLessonProgressDetail(
            @RequestParam("lessonId") Long lessonId,
            @RequestParam("profileId") Long profileId
    ) {
        LessonProgressDetailRes res = gradeProgressService.getLessonProgressDetail(profileId, lessonId);
        return ResponseEntity.ok(res);
    }
}
