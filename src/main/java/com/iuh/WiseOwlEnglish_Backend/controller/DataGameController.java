package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.MediaAssetImageDto;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.VocabOptionRes;
import com.iuh.WiseOwlEnglish_Backend.service.DataGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/data-game")
@RequiredArgsConstructor
public class DataGameController {
    private final DataGameService dataGameService;

    @GetMapping("/get-list-img/{lessonId}")
    public ResponseEntity<List<MediaAssetImageDto>> getImgsOfVocabsByLessonId(@PathVariable long lessonId){
        List<MediaAssetImageDto> res = dataGameService.getImgsOfVocabsByLessonId(lessonId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/get-list-vocab/{lessonId}")
    public ResponseEntity<List<VocabOptionRes>> getVocabsOptionByLessonId(@PathVariable long lessonId){
        List<VocabOptionRes> res = dataGameService.getListVocabOptionByLessonId(lessonId);
        return ResponseEntity.ok(res);
    }
}
