package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.CreateVocabReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.enums.MediaType;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.MediaAssetRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabServiceAdmin {
    private final VocabularyRepository vocabularyRepository;
    private final LessonRepository lessonRepository;
    private final MediaAssetRepository mediaAssetRepository;

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

    @Transactional
    public VocabRes createVocab(CreateVocabReq req){
        Vocabulary createdVocab=null;

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setTerm_en(req.getTerm_en());
        vocabulary.setTerm_vi(req.getTerm_vn());
        vocabulary.setPhonetic(req.getPhonetic());
        vocabulary.setOrderIndex(req.getOrderIndex());
        vocabulary.setPartOfSpeech(req.getPartOfSpeech());
        vocabulary.setCreatedAt(LocalDateTime.now());
        vocabulary.setUpdatedAt(LocalDateTime.now());
        Lesson lesson = lessonRepository.findById(req.getLessonId())
                .orElseThrow(()-> new NotFoundException("Khong tim thay lesson: "+req.getLessonId()));
        vocabulary.setLessonVocabulary(lesson);
        vocabulary.setForLearning(req.isForLearning());
        try {
             createdVocab = vocabularyRepository.save(vocabulary);
        }catch (Exception exception){
            throw new BadRequestException("Khong tao duoc vocab");
        }

        //Media IMG
        MediaAsset mediaImg = new MediaAsset();
        mediaImg.setUrl(req.getUrlImg());
        mediaImg.setMediaType(MediaType.IMAGE);
        mediaImg.setAltText(createdVocab.getTerm_en());
        mediaImg.setStorageProvider("Cloudinary");
        mediaImg.setTag("img");
        mediaImg.setCreatedAt(LocalDateTime.now());
        mediaImg.setUpdatedAt(LocalDateTime.now());
        mediaImg.setVocabulary(createdVocab);
        try {
            mediaAssetRepository.save(mediaImg);
        }catch (Exception exception){
            throw new BadRequestException("Khong tao duoc media img cho vocab");
        }

        //Media audio normal
        MediaAsset mediaAudioNormal = new MediaAsset();
        mediaAudioNormal.setUrl(req.getUrlAudioNormal());
        mediaAudioNormal.setMediaType(MediaType.AUDIO);
        mediaAudioNormal.setAltText(createdVocab.getTerm_en());
        mediaAudioNormal.setDurationSec(req.getDurationSecNormal());
        mediaAudioNormal.setStorageProvider("Cloudinary");
        mediaAudioNormal.setTag("normal");
        mediaAudioNormal.setCreatedAt(LocalDateTime.now());
        mediaAudioNormal.setUpdatedAt(LocalDateTime.now());
        mediaAudioNormal.setVocabulary(createdVocab);
        try {
            mediaAssetRepository.save(mediaAudioNormal);
        }catch (Exception exception){
            throw new BadRequestException("Khong tao duoc media audio normal cho vocab");
        }

        //Media audio slow
        MediaAsset mediaAudioSlow = new MediaAsset();
        mediaAudioSlow.setUrl(req.getUrlAudioSlow());
        mediaAudioSlow.setMediaType(MediaType.AUDIO);
        mediaAudioSlow.setAltText(createdVocab.getTerm_en());
        mediaAudioNormal.setDurationSec(req.getDurationSecSlow());
        mediaAudioSlow.setStorageProvider("Cloudinary");
        mediaAudioSlow.setTag("slow");
        mediaAudioSlow.setCreatedAt(LocalDateTime.now());
        mediaAudioSlow.setUpdatedAt(LocalDateTime.now());
        mediaAudioSlow.setVocabulary(createdVocab);
        try {
            mediaAssetRepository.save(mediaImg);
        }catch (Exception exception){
            throw new BadRequestException("Khong tao duoc media img cho vocab");
        }

        //map vocab to dto
        VocabRes res = new VocabRes();
        res.setId(createdVocab.getId());
        res.setOrderIndex(createdVocab.getOrderIndex());
        res.setTerm_en(createdVocab.getTerm_en());
        res.setPhonetic(createdVocab.getPhonetic());
        res.setPartOfSpeech(createdVocab.getPartOfSpeech());
        return res;

    }
}
