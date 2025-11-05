package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonByClassRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonByGradeRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonDTORS;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonsByAgeRes;
import com.iuh.WiseOwlEnglish_Backend.service.LessonQueryService;
import com.iuh.WiseOwlEnglish_Backend.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learn")
@RequiredArgsConstructor
public class LessonController {
    private final LessonQueryService service;
    private final LessonService lessonService;

    @GetMapping("/lessons/by-age")
    public ResponseEntity<LessonsByAgeRes> listLessonsByAge(
            @RequestParam("profileId") Long profileId,
            @AuthenticationPrincipal User principal
    ) {
        // TODO (khuyến nghị): verify profileId thuộc về principal.getUsername()
        LessonsByAgeRes res = service.getLessonsForProfileByAge(profileId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/lessons/home-page")
    public ResponseEntity<LessonByClassRes> listLessonForHomePage(
            @RequestParam("profileId") Long profileId,
            @AuthenticationPrincipal User principal
    ) {
        // TODO (khuyến nghị): verify profileId thuộc về principal.getUsername()
        LessonByClassRes res = lessonService.getLessonsForProfile(profileId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/lessons/by-grade")
    public ResponseEntity<List<LessonByGradeRes>> listLessonByGrade(@RequestParam long gradeId){
        List<LessonByGradeRes> res = lessonService.getListLessonByGrade(gradeId);
        return ResponseEntity.ok(res);
    }
}
