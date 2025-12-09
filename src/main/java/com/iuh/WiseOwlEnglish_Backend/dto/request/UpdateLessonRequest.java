package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLessonRequest {
    // Admin chỉ được gửi các trường này
    private String unitName;
    private String lessonName;
    private String mascot;
}
