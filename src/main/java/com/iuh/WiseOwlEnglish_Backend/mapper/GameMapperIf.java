package com.iuh.WiseOwlEnglish_Backend.mapper;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.GameResByLesson;
import com.iuh.WiseOwlEnglish_Backend.model.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GameMapperIf {
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    @Mapping(source = "type", target = "gameType")
    GameResByLesson gameToGameResByLesson(Game game);


    List<GameResByLesson> gamesToGameResByLessons(List<Game> games);
}
