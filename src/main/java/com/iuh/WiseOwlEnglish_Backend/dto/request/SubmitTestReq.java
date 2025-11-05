package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubmitTestReq {
    private Long learnerId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private List<AnswerReq> answers;
}
