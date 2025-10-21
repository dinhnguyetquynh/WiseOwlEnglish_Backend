package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProfileRes {
    private Long id;
    private String fullName;
    private String nickName;
    private Integer age;
    private String avatarUrl;
    private List<GradeProgressItem> gradeProgresses;


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class GradeProgressItem {
        private Long gradeLevelId;
        private String gradeName;
        private int orderIndex;
        private String status; // LOCKED / IN_PROGRESS / COMPLETED
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }
}
