package com.iuh.WiseOwlEnglish_Backend.mapper;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.GradeLevelDTO;
import com.iuh.WiseOwlEnglish_Backend.model.GradeLevel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GradeLevelMapper {


    GradeLevelDTO toDTO(GradeLevel gradeLevel);
//    GradeLevel toEntity(GradeLevelDTO gradeLevelDTO);
}
