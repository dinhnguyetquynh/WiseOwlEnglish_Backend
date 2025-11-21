package com.iuh.WiseOwlEnglish_Backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateSentenceReq {
    private String sen_en;
    private String sen_vn;
    private long lessonId;
    @JsonProperty("isForLearning")
    private boolean isForLearning;
    private String urlImg;
    private String urlAudioNormal;
    private int durationSecNormal;
    private String urlAudioSlow;
    private int durationSecSlow;
}
