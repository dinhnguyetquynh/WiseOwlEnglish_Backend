package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.TestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonWithTestsRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.TestAdminByLessonRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.TestsOfLessonRes;
import com.iuh.WiseOwlEnglish_Backend.service.TestAdminService;
import com.iuh.WiseOwlEnglish_Backend.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-admin")
@RequiredArgsConstructor
public class TestAdminController {
    private final TestAdminService testAdminService;

    @GetMapping("/get-all/{lessonId}")
    public ResponseEntity<TestsOfLessonRes> getAllTestByLesson(@PathVariable long lessonId){
        TestsOfLessonRes res = testAdminService.getTestsByLessonId(lessonId);
        return ResponseEntity.ok(res);
    }
    //API CHO ADMIN
    @GetMapping("/by-grade")
    public ResponseEntity<List<LessonWithTestsRes>> getTestsByGrade(@RequestParam Long gradeId) {
        List<LessonWithTestsRes> res = testAdminService.getTestsByGradeId(gradeId);
        return ResponseEntity.ok(res);
    }


    @PostMapping("/create")
    public ResponseEntity<TestRes> createTest(@RequestBody TestReq req) {
        TestRes res = testAdminService.createTest(req);
        return ResponseEntity.ok(res);
    }
}
