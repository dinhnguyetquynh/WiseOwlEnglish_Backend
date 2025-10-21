package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.SentenceMediaRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.SentenceRes;
import com.iuh.WiseOwlEnglish_Backend.mapper.MediaAssetMapper;
import com.iuh.WiseOwlEnglish_Backend.mapper.SentenceMapper;
import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import com.iuh.WiseOwlEnglish_Backend.model.Sentence;
import com.iuh.WiseOwlEnglish_Backend.repository.SentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SentenceService {
    private final SentenceRepository sentenceRepository;
    private final MediaAssetService  mediaAssetService;
    private final MediaAssetMapper mediaAssetMapper;
    private final SentenceMapper sentenceMapper;
    List<MediaAsset> mediaAssets;
    List<SentenceMediaRes> sentenceMediaResList;

    public List<SentenceMediaRes> getSentenceMediaResListByLessonId(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLessonSentenceIdOrderByOrderIndexAsc(lessonId);

        sentenceMediaResList = new ArrayList<>();
        for (Sentence sentence : sentences) {
          SentenceMediaRes sentenceMediaRes = new SentenceMediaRes();
            sentenceMediaRes.setId(sentence.getId());
            sentenceMediaRes.setOrderIndex(sentence.getOrderIndex());
            sentenceMediaRes.setSentence_en(sentence.getSentence_en());
            sentenceMediaRes.setSentence_vi(sentence.getSentence_vi());
            mediaAssets = mediaAssetService.getMediaAssetsBySentenceId(sentence.getId());
            sentenceMediaRes.setMediaAssets(mediaAssetMapper.toDtoList(mediaAssets));
            sentenceMediaResList.add(sentenceMediaRes);
        }
        return sentenceMediaResList;

    }

}
