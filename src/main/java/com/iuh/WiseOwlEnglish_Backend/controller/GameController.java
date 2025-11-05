package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.GameReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.service.GameService;
import com.iuh.WiseOwlEnglish_Backend.service.GameServiceAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final GameServiceAdmin gameServiceAdmin;

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

    //api for admin
    @PostMapping("/create-game")
    public ResponseEntity<GameRes> createGame(@Validated @RequestBody GameReq req){
        GameRes res = gameServiceAdmin.createGame(req);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/picture-word")
    public ResponseEntity<List<PictureWordRes>> getPictureWordGame(@RequestParam long lessonId){
        List<PictureWordRes> games = gameService.getListGamePictureWordWriting(lessonId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/picture-match-word")
    public ResponseEntity<List<PictureMatchWordRes>> getPictureMatchWordGame(@RequestParam long lessonId){
        List<PictureMatchWordRes> games = gameService.getGamesPictureMatchWord(lessonId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/sentence-hidden")
    public ResponseEntity<List<SentenceHiddenRes>> getSentenceHiddenGame(@RequestParam long lessonId){
        List<SentenceHiddenRes> games = gameService.getGamesSentenceHidden(lessonId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/word-to-sentence")
    public ResponseEntity<List<WordToSentenceRes>> getWordToSentenceGame(@RequestParam long lessonId){
        List<WordToSentenceRes> games = gameService.getWordToSentenceGame(lessonId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<GameResByLesson>> getGamesByLesson(@RequestParam long lessonId){
        List<GameResByLesson> games = gameServiceAdmin.getListGameByLesson(lessonId);
        return ResponseEntity.ok(games);
    }





}
