package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearnerStatsRes {
    private long totalLearners;
    private long totalUserAccounts;
    private List<MonthlyGrowth> monthlyGrowth;
    private List<GradeDistribution> gradeDistribution;

    @Data
    @AllArgsConstructor
    public static class MonthlyGrowth {
        private String month; // "10/2023"
        private long count;
    }

//    @Data
//    @AllArgsConstructor
//    public static class GradeDistribution {
//        private String gradeName;
//        private long count;
//    }
}
