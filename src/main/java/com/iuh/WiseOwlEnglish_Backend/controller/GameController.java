package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.PictureGuessingGameRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.PictureSentenceQuesRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.SoundWordOptionRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.SoundWordQuestionRes;
import com.iuh.WiseOwlEnglish_Backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @GetMapping("/picture-guessing")
    public ResponseEntity<List<PictureGuessingGameRes>> getPictureGuessingGames(@RequestParam long lessonId) {
        List<PictureGuessingGameRes> games = gameService.getListGamePictureGuessing(lessonId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/sound-word")
    public ResponseEntity<List<SoundWordQuestionRes>> getSoundWordGames(@RequestParam long lessonId) {
        List<SoundWordQuestionRes> games = gameService.getListGameSoundWord(lessonId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/picture-sentence")
    public ResponseEntity<List<PictureSentenceQuesRes>> getPictureSentenceGames(@RequestParam long lessonId) {
        List<PictureSentenceQuesRes> games = gameService.getListGamePictureSentence(lessonId);
        return ResponseEntity.ok(games);
    }
}
