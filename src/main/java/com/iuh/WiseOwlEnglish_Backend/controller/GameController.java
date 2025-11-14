package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.GameAnswerReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.GameReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.service.GameService;
import com.iuh.WiseOwlEnglish_Backend.service.GameServiceAdmin;
import jakarta.validation.Valid;
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

    //API LẤY DANH SÁCH GAME CỦA CÁC LESSON thuoc 1 lop(grade)
    @GetMapping("/lesson-games-by-grade")
    public ResponseEntity<List<LessonWithGamesDTO>> getLessonsWithGamesByGrade(@RequestParam long gradeId) {
        List<LessonWithGamesDTO> res = gameServiceAdmin.getLessonsWithGamesByGrade(gradeId);
        return ResponseEntity.ok(res);
    }

    //Xem Chi tiet  cac game thuoc 1 lesson
    @GetMapping("/details-games-of-lesson/{lessonId}")
    public ResponseEntity<GamesOfLessonRes> getGamesDetailByLesson(@PathVariable long lessonId) {
        // Giả sử logic service của bạn nằm trong GameService
        GamesOfLessonRes res = gameServiceAdmin.getGamesDetailByLesson(lessonId);
        return ResponseEntity.ok(res);
    }

    //api trả về các loại game chưa có trong lesson cho Admin
    @GetMapping("/types-by-grade")
    public ResponseEntity<List<String>> getGameTypesByGrade(
            @RequestParam int gradeOrder,
            @RequestParam long lessonId
    ) {
        List<String> types = gameServiceAdmin.getGameTypesByGrade(gradeOrder, lessonId);
        return ResponseEntity.ok(types);
    }

    @GetMapping("/review-list")
    public ResponseEntity<List<GameResByLesson>> getReviewGames(
            @RequestParam long lessonId,
            @RequestParam String category // "vocab" or "sentence"
    ) {
        List<GameResByLesson> games = gameService.getGamesForReview(lessonId, category);
        return ResponseEntity.ok(games);
    }

    @PostMapping("/submit-answer")
    public ResponseEntity<GameAnswerRes> submitGameAnswer(
            @Valid @RequestBody GameAnswerReq req
    ) {
        GameAnswerRes res = gameService.submitAnswer(req);
        return ResponseEntity.ok(res);
    }

}
