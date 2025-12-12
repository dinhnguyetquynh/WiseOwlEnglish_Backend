package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

@Data
public class SentenceUpdateReq {
    private String sen_en;
    private String sen_vi;
    private String imgUrl;
    private String audioNormal;
    private String audioSlow;
    private boolean isForLearning;
}
