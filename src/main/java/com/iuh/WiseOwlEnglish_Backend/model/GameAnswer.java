package com.iuh.WiseOwlEnglish_Backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "game_answer",
        indexes = {
                @Index(name = "idx_gans_attempt", columnList = "attempt_id"),
                @Index(name = "idx_gans_question", columnList = "question_id"),
                @Index(name = "idx_gans_option", columnList = "option_id"),
                @Index(name = "idx_gans_option_left", columnList = "option_left_id"),
                @Index(name = "idx_gans_option_right", columnList = "option_right_id")
        }
)
public class GameAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "attempt_id", nullable = false)
    private GameAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "question_id", nullable = false)
    private GameQuestion gameQuestion;

    // Dạng chọn 1/đúng-sai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", foreignKey = @ForeignKey(name = "fk_answer_option"))
    private GameOption option; // FK tới game_option.id (có thể thêm @ManyToOne nếu bạn muốn)

    // Dạng matching (nối cặp trái-phải)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_left_id", foreignKey = @ForeignKey(name = "fk_answer_left_option"))
    private GameOption optionLeft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_right_id", foreignKey = @ForeignKey(name = "fk_answer_right_option"))
    private GameOption optionRight;
    // Dạng điền khuyết
    @Column(name = "answer_text")
    private String answerText;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    private LocalDateTime createdAt;
}
