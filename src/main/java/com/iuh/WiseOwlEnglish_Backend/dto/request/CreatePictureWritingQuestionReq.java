package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreatePictureWritingQuestionReq {
    private Long lessonId;
    private Long imageMediaId;           // ảnh gợi ý
    private String mainAnswer;            // đáp án chính, vd: "plane"
    private List<String> synonyms;        // tùy chọn: "airplane", "aeroplane"
    private Integer position;        // tùy chọn; nếu null thì auto tăng
    private Integer rewardScore;           // điểm thưởng (nếu null dùng default)
}
