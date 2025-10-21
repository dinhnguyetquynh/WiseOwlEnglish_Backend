package com.iuh.WiseOwlEnglish_Backend.model;

import com.iuh.WiseOwlEnglish_Backend.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "game_attempt",
        indexes = {
                @Index(name = "idx_ga_learner", columnList = "learner_id"),
                @Index(name = "idx_ga_game", columnList = "game_id")
        }
)
public class GameAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="learner_id",nullable = false)
    private LearnerProfile learnerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AttemptStatus status;

    @Column(name = "reward_count")
    private Integer rewardCount;

    @Column(name = "wrong_count")
    private Integer wrongCount;

    @Column(name = "current_question_id")
    private Long currentQuestionId;   // Lưu ID của câu game hiện tại đang chơi, lan sau vao lai se hien thi lai

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
