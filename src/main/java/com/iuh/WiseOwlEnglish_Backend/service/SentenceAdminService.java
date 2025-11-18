package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.SentenceAdminRes;
import com.iuh.WiseOwlEnglish_Backend.model.Sentence;
import com.iuh.WiseOwlEnglish_Backend.repository.SentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SentenceAdminService {
    private final SentenceRepository repository;

    public List<SentenceAdminRes> getListSentence(long lessonId){
        List<Sentence> sentenceList = repository.findByLessonSentence_Id(lessonId);

        List<SentenceAdminRes> sentenceResList = new ArrayList<>();
        for(Sentence sentence: sentenceList){
            SentenceAdminRes res = toDTO(sentence);
            sentenceResList.add(res);
        }
        return sentenceResList;
    }

    private SentenceAdminRes toDTO(Sentence sentence){
        SentenceAdminRes res = new SentenceAdminRes();
        res.setId(sentence.getId());
        res.setOrderIndex(sentence.getOrderIndex());
        res.setSen_en(sentence.getSentence_en());
        return res;
    }
}
