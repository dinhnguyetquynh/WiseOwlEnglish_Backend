package com.iuh.WiseOwlEnglish_Backend.mapper;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonDTORS;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    LessonMapper INSTANCE = Mappers.getMapper(LessonMapper.class);

    LessonDTORS toDTO(Lesson lesson);
//    Lesson toEntity(LessonDTORS lessonDTORS);
}
