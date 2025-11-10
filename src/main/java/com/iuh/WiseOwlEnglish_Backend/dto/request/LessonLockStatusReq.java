package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

@Data
public class LessonLockStatusReq {
    private long lessonId;
    private long profileId;
}
