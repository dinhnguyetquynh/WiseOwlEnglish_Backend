package com.iuh.WiseOwlEnglish_Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VocabUpdateReq {
    private String term_en;
    private String term_vi;
    private String phonetic;
    private String partOfSpeech;
    private String imgUrl;
    private String audioNormal;
    private String audioSlow;
    private boolean isForLearning;
}