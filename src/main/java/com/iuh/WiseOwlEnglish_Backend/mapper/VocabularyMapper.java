package com.iuh.WiseOwlEnglish_Backend.mapper;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.VocabularyDTORes;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = { MediaAssetMapper.class })
public interface VocabularyMapper {

//    Vocabulary toEntity(VocabularyDTORes vocabularyDTORes);
    VocabularyDTORes toDTO(Vocabulary vocabulary);

    List<VocabularyDTORes> toDtoList(List<Vocabulary> entities);
}
