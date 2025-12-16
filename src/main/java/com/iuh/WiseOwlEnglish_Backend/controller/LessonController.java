package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.service.LessonQueryService;
import com.iuh.WiseOwlEnglish_Backend.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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


    @GetMapping("/lessons/for-guest/{gradeId}")
    public ResponseEntity<LessonByClassRes> getLessonsForGuest(@PathVariable long gradeId){
        LessonByClassRes res = lessonService.getLessonForGuest(gradeId);
        return ResponseEntity.ok(res);
    }


    @GetMapping("/lessons/home-page")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<LessonByClassRes> listLessonForHomePage(
            @RequestParam("profileId") Long profileId,
            @AuthenticationPrincipal User principal
    ) {
        // TODO (khuyến nghị): verify profileId thuộc về principal.getUsername()
        LessonByClassRes res = lessonService.getLessonsForProfile(profileId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/lessons/by-grade")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<List<LessonByGradeRes>> listLessonByGrade(@RequestParam long gradeId) {
        List<LessonByGradeRes> res = lessonService.getListLessonByGrade(gradeId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/lessons/by-grade-for-profile")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<LessonByClassRes> listLessonByGradeForProfile(
            @RequestParam("profileId") Long profileId,
            @RequestParam("gradeOrderIndex") int gradeOrderIndex,
            @AuthenticationPrincipal User principal
    ) {
        // TODO (khuyến nghị): verify profileId thuộc về principal.getUsername()
        LessonByClassRes res = lessonService.getLessonsByGradeForProfile(profileId, gradeOrderIndex);
        return ResponseEntity.ok(res);
    }


}
