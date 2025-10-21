package com.iuh.WiseOwlEnglish_Backend.mapper;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.SentenceRes;
import com.iuh.WiseOwlEnglish_Backend.model.Sentence;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",uses = {SentenceMapper.class})
public interface SentenceMapper {
    SentenceRes toDTO(Sentence sentence);
}
