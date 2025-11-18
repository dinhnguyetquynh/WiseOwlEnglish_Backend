package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLessonReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.CreateLessonRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.SentenceRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonDetail;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.SentenceAdminRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.service.LessonAdminService;
import com.iuh.WiseOwlEnglish_Backend.service.SentenceAdminService;
import com.iuh.WiseOwlEnglish_Backend.service.VocabServiceAdmin;
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
    private final VocabServiceAdmin vocabServiceAdmin;
    private final SentenceAdminService sentenceAdminService;

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

    @GetMapping("/detail/{lessonId}")
    public  ResponseEntity<LessonDetail> getDetailLesson(@PathVariable long lessonId){
        List<VocabRes> vocabResList = vocabServiceAdmin.getListVocab(lessonId);
        List<SentenceAdminRes> sentenceResList = sentenceAdminService.getListSentence(lessonId);

        LessonDetail detail = new LessonDetail();
        detail.setVocabResList(vocabResList);
        detail.setSentenceResList(sentenceResList);

        return ResponseEntity.ok(detail);
    }
}
