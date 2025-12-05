package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.GradeReportRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.LearnerStatsRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.LessonStatsRes;
import com.iuh.WiseOwlEnglish_Backend.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {
    private final AdminStatsService statsService;

    @GetMapping("/learners")
    public ResponseEntity<LearnerStatsRes> getLearnerStats(
            @RequestParam(defaultValue = "2025") int year // Mặc định năm hiện tại hoặc tùy chọn
    ) {
        return ResponseEntity.ok(statsService.getLearnerStats(year));
    }

    @GetMapping("/lessons-by-grade/{gradeId}")
    public ResponseEntity<GradeReportRes> getLessonStats(@PathVariable Long gradeId) { // 👈 Đổi kiểu trả về
        return ResponseEntity.ok(statsService.getLessonStatsByGrade(gradeId));
    }
}
