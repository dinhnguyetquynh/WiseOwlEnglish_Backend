package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.*;
import com.iuh.WiseOwlEnglish_Backend.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {
    private final AdminStatsService statsService;

    @GetMapping("/learners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LearnerStatsRes> getLearnerStats(
            @RequestParam(defaultValue = "2025") int year // Mặc định năm hiện tại hoặc tùy chọn
    ) {
        return ResponseEntity.ok(statsService.getLearnerStats(year));
    }

    @GetMapping("/lessons-by-grade/{gradeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GradeReportRes> getLessonStats(@PathVariable Long gradeId) { // 👈 Đổi kiểu trả về
        return ResponseEntity.ok(statsService.getLessonStatsByGrade(gradeId));
    }

    @GetMapping("/total-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataRes> getTotalData(){
        return ResponseEntity.ok(statsService.getTotalData());
    }

    @GetMapping("/learning-activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyStatRes>> getLearningActivity(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(statsService.getLearningActivityStats(startDate, endDate));
    }


}
