package com.iuh.WiseOwlEnglish_Backend.model;

import com.iuh.WiseOwlEnglish_Backend.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "learner_grade_progress",
        indexes = {
                @Index(name = "idx_lgp_learner", columnList = "learner_id"),
                @Index(name = "idx_lgp_grade", columnList = "grade_level_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_lgp_learner_grade", columnNames = {"learner_id", "grade_level_id"})
        }
)
public class LearnerGradeProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Học viên
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LearnerProfile learnerProfile;

    // Lớp
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_level_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GradeLevel gradeLevel;

    // Trạng thái học lớp này
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status; // LOCKED / IN_PROGRESS / COMPLETED

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary; // Lớp chính (true) hay lớp phụ (false)
}
