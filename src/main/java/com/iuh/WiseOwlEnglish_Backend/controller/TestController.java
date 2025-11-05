package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.SubmitTestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.TestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.SubmitTestRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestResByLesson;
import com.iuh.WiseOwlEnglish_Backend.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @PostMapping("/create")
    public ResponseEntity<TestRes> createTest(@RequestBody TestReq req) {
        TestRes res = testService.createTest(req);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/get-test/{id}")
    public ResponseEntity<TestRes> getTestById(@PathVariable Long id){
        TestRes res = testService.getTestById(id);
        return ResponseEntity.ok(res);

    }

    @PostMapping("/{testId}/submit")
    public ResponseEntity<SubmitTestRes> submit(
            @PathVariable Long testId,
            @RequestBody SubmitTestReq req
    ) {
        Long learnerId = req.getLearnerId();
        SubmitTestRes res = testService.submitAndGrade(learnerId, testId, req);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/get-list/{lessonId}")
    public ResponseEntity<List<TestResByLesson>> getListTest(@PathVariable Long lessonId){
        List<TestResByLesson> res = testService.getTestsByLessonId(lessonId);
        return ResponseEntity.ok(res);

    }
}

