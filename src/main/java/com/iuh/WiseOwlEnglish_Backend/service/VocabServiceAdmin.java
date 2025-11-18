package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import com.iuh.WiseOwlEnglish_Backend.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabServiceAdmin {
    private final VocabularyRepository vocabularyRepository;

    public List<VocabRes> getListVocab(long lessonId){
        List<Vocabulary> vocabularyList = vocabularyRepository.findByLessonVocabulary_Id(lessonId);
        List<VocabRes> vocabResList = new ArrayList<>();
        for(Vocabulary vocabulary: vocabularyList){
            VocabRes res = toDTO(vocabulary);
            vocabResList.add(res);
        }
        return vocabResList;
    }

    private VocabRes toDTO(Vocabulary vocabulary){
        VocabRes vocabRes = new VocabRes();
        vocabRes.setId(vocabulary.getId());
        vocabRes.setOrderIndex(vocabulary.getOrderIndex());
        vocabRes.setTerm_en(vocabulary.getTerm_en());
        vocabRes.setPartOfSpeech(vocabulary.getPartOfSpeech());
        return vocabRes;
    }
}
