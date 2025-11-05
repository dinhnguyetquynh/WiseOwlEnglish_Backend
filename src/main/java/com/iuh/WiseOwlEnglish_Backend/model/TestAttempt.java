package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iuh.WiseOwlEnglish_Backend.enums.TestAttemptStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "test_attempt",
        indexes = {
                @Index(name = "idx_ta_learner", columnList = "learner_id"),
                @Index(name = "idx_ta_test", columnList = "test_id")
        }
)
public class TestAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", foreignKey = @ForeignKey(name = "fk_attempt_learner"))
    private LearnerProfile learnerProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", foreignKey = @ForeignKey(name = "fk_attempt_test"))
    private Test test;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    private Double score;                 // tổng điểm
    @Column(name = "correct_count")
    private Integer correctCount;
    @Column(name = "wrong_count")
    private Integer wrongCount;
    @Column(name = "question_count")
    private Integer questionCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private TestAttemptStatus status;

    //thoi gian lam bai
    @Column(name = "duration_min")
    private Integer durationMin;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TestAnswer> answers;
}
