package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

@Data
public class CreateLessonReq {
    private String unitNumber;
    private String unitName;
    private int orderIndex;
    private boolean active;
    private long gradeLevelId;
    private String urlMascot;
}
