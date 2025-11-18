package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateVocabReq {
    private String term_en;
    private String term_vn;
    private String phonetic;
    private String partOfSpeech;
    private int orderIndex;
    private long lessonId;
    @JsonProperty("isForLearning")
    private boolean isForLearning;
    private String urlImg;
    private String urlAudioNormal;
    private int durationSecNormal;
    private String urlAudioSlow;
    private int durationSecSlow;

}
