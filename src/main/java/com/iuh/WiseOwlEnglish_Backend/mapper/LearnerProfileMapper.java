package com.iuh.WiseOwlEnglish_Backend.mapper;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.LearnerProfileRes;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerProfile;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface LearnerProfileMapper {
    List<LearnerProfileRes> toDTOs(List<LearnerProfile> learnerProfiles);

    LearnerProfileRes toDTO(LearnerProfile profile);

}
