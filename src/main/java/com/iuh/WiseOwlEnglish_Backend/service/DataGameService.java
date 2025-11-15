package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.DataGameForAdmin;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.MediaAssetForAdminDto;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.OptionsRes;
import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.enums.MediaType;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.model.Sentence;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.MediaAssetRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.SentenceRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataGameService {
    private final MediaAssetRepository mediaAssetRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;

    public List<MediaAssetForAdminDto> getImgsOfVocabsByLessonId(long lessonId){
        List<MediaAssetForAdminDto> imageDtos =mediaAssetRepository.findAssetVocabByLessonId(MediaType.IMAGE,lessonId);
        if (imageDtos == null || imageDtos.isEmpty()) {
            throw new NotFoundException(
                    "Không tìm thấy hình ảnh nào cho các từ vựng thuộc lessonId = " + lessonId
            );
        }

        return imageDtos;
    }

    public List<MediaAssetForAdminDto> getAudiosOfVocabsByLessonId(long lessonId){
        List<MediaAssetForAdminDto> imageDtos =mediaAssetRepository.findAssetVocabByLessonId(MediaType.AUDIO,lessonId);
        if (imageDtos == null || imageDtos.isEmpty()) {
            throw new NotFoundException(
                    "Không tìm thấy hình ảnh nào cho các từ vựng thuộc lessonId = " + lessonId
            );
        }
        return imageDtos;
    }

    public List<MediaAssetForAdminDto> getImgOfSensByLessonId(long lessonId){
        List<MediaAssetForAdminDto> imageDtos =mediaAssetRepository.findAssetSentenceBySentenceLessonId(MediaType.IMAGE,lessonId);
        if (imageDtos == null || imageDtos.isEmpty()) {
            throw new NotFoundException(
                    "Không tìm thấy hình ảnh nào của các câu thuộc lessonId = " + lessonId
            );
        }
        return imageDtos;
    }

    public List<OptionsRes> getListVocabOptionByLessonId(long lessonId){
        List<Vocabulary> vocabularyList = vocabularyRepository.findByLessonVocabulary_Id(lessonId);

        if (vocabularyList == null || vocabularyList.isEmpty()) {
            throw new NotFoundException(
                    "Không tìm thấy các từ vựng thuộc lessonId = " + lessonId
            );
        }
        List<OptionsRes> optionResList = new ArrayList<>();
        for(Vocabulary vocabulary:vocabularyList){
            OptionsRes res = new OptionsRes();
            res.setId(vocabulary.getId());
            res.setTerm_en(vocabulary.getTerm_en());
            optionResList.add(res);
        }
        return optionResList;
    }

    public List<OptionsRes> getListSenOptionByLessonId(long lessonId){
        List<Sentence> sentencesList = sentenceRepository.findByLessonSentenceIdOrderByOrderIndexAsc(lessonId);

        if (sentencesList  == null || sentencesList .isEmpty()) {
            throw new NotFoundException(
                    "Không tìm thấy các câu thuộc lessonId = " + lessonId
            );
        }
        List<OptionsRes> optionResList = new ArrayList<>();
        for(Sentence sentence:sentencesList){
            OptionsRes res = new OptionsRes();
            res.setId(sentence.getId());
            res.setTerm_en(sentence.getSentence_en());
            optionResList.add(res);
        }
        return optionResList;
    }

    @Transactional(readOnly = true)
    public DataGameForAdmin getDataGameForAdmin(GameType gameType, long lessonId) {
        DataGameForAdmin data = new DataGameForAdmin();

        //trả ve title: UNIT 2 : COLORS / TẠO TRÒ CHƠI “NHÌN HÌNH CHỌN TỪ
        Lesson lesson = lessonRepository.findById(lessonId).
                orElseThrow(()->new NotFoundException("Khong tim thay bai hoc"));
        // an toàn hoá unit/lesson name
        String unitNumber = Optional.ofNullable(lesson.getUnitName()).map(String::trim).filter(s -> !s.isEmpty()).orElse("UNIT");
        String unitName = Optional.ofNullable(lesson.getLessonName()).map(String::trim).orElse("");

        // xây title bằng switch — dễ maintain hơn
        String titleSuffix;
        switch (gameType) {
            case PICTURE_WORD_MATCHING:
                titleSuffix = "TẠO TRÒ CHƠI “NHÌN HÌNH CHỌN TỪ VỰNG”";
                break;
            case PICTURE_WORD_WRITING:
                titleSuffix = "TẠO TRÒ CHƠI “NHÌN HÌNH - VIẾT TỪ VỰNG”";
                break;
            case PICTURE4_WORD4_MATCHING:
                titleSuffix = "TẠO TRÒ CHƠI “NỐI HÌNH VỚI TỪ VỰNG”";
                break;
            case SOUND_WORD_MATCHING:
                titleSuffix = "NGHE PHÁT ÂM CHỌN TỪ VỰNG";
                break;
            case PICTURE_SENTENCE_MATCHING:
                titleSuffix = "NHÌN HÌNH CHỌN CÂU";
                break;
            case SENTENCE_HIDDEN_WORD:
                titleSuffix = "ĐIỀN TỪ CÒN THIẾU TRONG CÂU";
                break;
            case WORD_TO_SENTENCE:
                titleSuffix = "SẮP XẾP TỪ THÀNH CÂU";
                break;
            default:
                titleSuffix = "";
        }

        String title = unitNumber + (unitName.isEmpty() ? "" : " : " + unitName) + (titleSuffix.isEmpty() ? "" : " / " + titleSuffix);
        data.setTitle(title);

        switch (gameType) {
            case PICTURE_WORD_MATCHING:
            case PICTURE_WORD_WRITING:
            case PICTURE4_WORD4_MATCHING:
                data.setMediaAssets(nonNullList(getImgsOfVocabsByLessonId(lessonId)));
                data.setOptions(nonNullList(getListVocabOptionByLessonId(lessonId)));
                break;

            case SOUND_WORD_MATCHING:
                data.setMediaAssets(nonNullList(getAudiosOfVocabsByLessonId(lessonId)));
                data.setOptions(nonNullList(getListVocabOptionByLessonId(lessonId)));
                break;

            case PICTURE_SENTENCE_MATCHING:
                data.setMediaAssets(nonNullList(getImgOfSensByLessonId(lessonId)));
                data.setOptions(nonNullList(getListSenOptionByLessonId(lessonId)));
                break;

            case SENTENCE_HIDDEN_WORD:
                data.setMediaAssets(nonNullList(getImgsOfVocabsByLessonId(lessonId)));
                data.setOptions(nonNullList(getListSenOptionByLessonId(lessonId)));
                break;

            case WORD_TO_SENTENCE:
                data.setOptions(nonNullList(getListSenOptionByLessonId(lessonId)));
                break;

            default:
                // optional: set empty lists explicitly
                data.setMediaAssets(Collections.emptyList());
                data.setOptions(Collections.emptyList());
        }

        return data;
    }

    private <T> List<T> nonNullList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
