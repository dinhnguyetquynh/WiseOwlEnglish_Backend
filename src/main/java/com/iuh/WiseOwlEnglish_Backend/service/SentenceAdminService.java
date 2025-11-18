package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLessonReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateSentenceReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.SentenceAdminRes;
import com.iuh.WiseOwlEnglish_Backend.model.Sentence;
import com.iuh.WiseOwlEnglish_Backend.repository.SentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

//    @Transactional
//    public SentenceAdminRes createSentence(CreateSentenceReq req){
//        Sentence createdSen=null;
//
//        Sentence sentence = new Sentence();
//        sentence.setOrderIndex(req.getOrderIndex());
//        sentence.setSentence_en(req.getSen_en());
//        sentence.setSentence_vi(req.getSen_vn());
//        sentence.setCreatedAt(LocalDateTime.now());
//        sentence.setUpdatedAt();
//        try {
//            createdVocab = vocabularyRepository.save(vocabulary);
//        }catch (Exception exception){
//            throw new BadRequestException("Khong tao duoc vocab");
//        }
//
//        //Media IMG
//        MediaAsset mediaImg = new MediaAsset();
//        mediaImg.setUrl(req.getUrlImg());
//        mediaImg.setMediaType(MediaType.IMAGE);
//        mediaImg.setAltText(createdVocab.getTerm_en());
//        mediaImg.setStorageProvider("Cloudinary");
//        mediaImg.setTag("img");
//        mediaImg.setCreatedAt(LocalDateTime.now());
//        mediaImg.setUpdatedAt(LocalDateTime.now());
//        mediaImg.setVocabulary(createdVocab);
//        try {
//            mediaAssetRepository.save(mediaImg);
//        }catch (Exception exception){
//            throw new BadRequestException("Khong tao duoc media img cho vocab");
//        }
//
//        //Media audio normal
//        MediaAsset mediaAudioNormal = new MediaAsset();
//        mediaAudioNormal.setUrl(req.getUrlAudioNormal());
//        mediaAudioNormal.setMediaType(MediaType.AUDIO);
//        mediaAudioNormal.setAltText(createdVocab.getTerm_en());
//        mediaAudioNormal.setDurationSec(req.getDurationSecNormal());
//        mediaAudioNormal.setStorageProvider("Cloudinary");
//        mediaAudioNormal.setTag("normal");
//        mediaAudioNormal.setCreatedAt(LocalDateTime.now());
//        mediaAudioNormal.setUpdatedAt(LocalDateTime.now());
//        mediaAudioNormal.setVocabulary(createdVocab);
//        try {
//            mediaAssetRepository.save(mediaAudioNormal);
//        }catch (Exception exception){
//            throw new BadRequestException("Khong tao duoc media audio normal cho vocab");
//        }
//
//        //Media audio slow
//        MediaAsset mediaAudioSlow = new MediaAsset();
//        mediaAudioSlow.setUrl(req.getUrlAudioSlow());
//        mediaAudioSlow.setMediaType(MediaType.AUDIO);
//        mediaAudioSlow.setAltText(createdVocab.getTerm_en());
//        mediaAudioNormal.setDurationSec(req.getDurationSecSlow());
//        mediaAudioSlow.setStorageProvider("Cloudinary");
//        mediaAudioSlow.setTag("slow");
//        mediaAudioSlow.setCreatedAt(LocalDateTime.now());
//        mediaAudioSlow.setUpdatedAt(LocalDateTime.now());
//        mediaAudioSlow.setVocabulary(createdVocab);
//        try {
//            mediaAssetRepository.save(mediaImg);
//        }catch (Exception exception){
//            throw new BadRequestException("Khong tao duoc media img cho vocab");
//        }
//
//        //map vocab to dto
//        VocabRes res = new VocabRes();
//        res.setId(createdVocab.getId());
//        res.setOrderIndex(createdVocab.getOrderIndex());
//        res.setTerm_en(createdVocab.getTerm_en());
//        res.setPhonetic(createdVocab.getPhonetic());
//        res.setPartOfSpeech(createdVocab.getPartOfSpeech());
//        return res;
//    }
}
