package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;
import org.apache.catalina.LifecycleState;

import java.util.List;

@Data
public class TestReq {
    private Long lessonId;
    private String title;
    private String type;
    private String description;
    private Integer durationMin;
    private Boolean active;
    List<TestQuestionReq> questions;
}
