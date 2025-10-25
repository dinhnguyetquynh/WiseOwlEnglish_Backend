package com.iuh.WiseOwlEnglish_Backend.mapper;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.GameRes;
import com.iuh.WiseOwlEnglish_Backend.model.Game;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameRes toDTO(Game savedGame){
        GameRes res = new GameRes();
        res.setId(savedGame.getId());
        res.setTitle(savedGame.getTitle());
        res.setType(savedGame.getType().toString());
        res.setDifficulty(savedGame.getDifficulty());
        res.setLessonId(savedGame.getLesson().getId());
        res.setCorrectAudio(savedGame.getCorrectAudio().getId());
        res.setWrongAudio(savedGame.getWrongAudio().getId());

        return res;
    }
}
