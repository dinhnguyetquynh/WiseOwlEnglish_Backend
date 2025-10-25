package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.TestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestRes;
import com.iuh.WiseOwlEnglish_Backend.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

