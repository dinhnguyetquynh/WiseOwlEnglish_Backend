package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.MediaAssetImageDto;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.VocabOptionRes;
import com.iuh.WiseOwlEnglish_Backend.enums.MediaType;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import com.iuh.WiseOwlEnglish_Backend.repository.MediaAssetRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataGameService {
    private final MediaAssetRepository mediaAssetRepository;
    private final VocabularyRepository vocabularyRepository;

    public List<MediaAssetImageDto> getImgsOfVocabsByLessonId(long lessonId){
        List<MediaAssetImageDto> imageDtos =mediaAssetRepository.findImageDtosByLessonId(MediaType.IMAGE,lessonId);
        if (imageDtos == null || imageDtos.isEmpty()) {
            throw new NotFoundException(
                    "Không tìm thấy hình ảnh nào cho các từ vựng thuộc lessonId = " + lessonId
            );
        }

        return imageDtos;
    }

    public List<VocabOptionRes> getListVocabOptionByLessonId(long lessonId){
        List<Vocabulary> vocabularyList = vocabularyRepository.findByLessonVocabulary_Id(lessonId);

        if (vocabularyList == null || vocabularyList.isEmpty()) {
            throw new NotFoundException(
                    "Không tìm thấy các từ vựng thuộc lessonId = " + lessonId
            );
        }
        List<VocabOptionRes> optionResList = new ArrayList<>();
        for(Vocabulary vocabulary:vocabularyList){
            VocabOptionRes res = new VocabOptionRes();
            res.setId(vocabulary.getId());
            res.setTerm_en(vocabulary.getTerm_en());
            optionResList.add(res);
        }
        return optionResList;
    }
}
