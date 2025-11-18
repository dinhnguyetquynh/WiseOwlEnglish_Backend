package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonRes;
import com.iuh.WiseOwlEnglish_Backend.service.LessonAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lesson-admin")
@RequiredArgsConstructor
public class LessonAdminController {
    private final LessonAdminService adminService;

    @GetMapping("/get-list/{gradeId}")
    public ResponseEntity<List<LessonRes>> getListLesson(@PathVariable long gradeId) {
        List<LessonRes> res = adminService.getListLessonByGradeId(gradeId);
        return ResponseEntity.ok(res);
    }
}
