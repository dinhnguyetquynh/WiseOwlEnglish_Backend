package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import lombok.Data;

import java.util.List;

@Data
public class LessonDetail {
    List<VocabRes> vocabResList;
    List<SentenceAdminRes> sentenceResList;
}
