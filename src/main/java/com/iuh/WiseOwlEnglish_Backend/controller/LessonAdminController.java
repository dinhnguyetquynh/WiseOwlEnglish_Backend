package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLessonReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.CreateLessonRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonRes;
import com.iuh.WiseOwlEnglish_Backend.service.LessonAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    public ResponseEntity<CreateLessonRes> create(@RequestBody CreateLessonReq req) {
        CreateLessonRes created = adminService.createLesson(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
