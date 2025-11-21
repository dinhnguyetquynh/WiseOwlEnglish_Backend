package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

@Data
public class TestOptionReq {
    private String contentType;     // TEXT/VOCAB/SENTENCE/IMAGE/AUDIO
    private Long contentRefId;
    private String text;                 // khi contentType = TEXT (có thể trống)
    private boolean correct;             // cho trắc nghiệm/true-false
    private String side;                   // LEFT/RIGHT
    private String pairKey;              // mã ghép cặp
}
