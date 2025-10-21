package com.iuh.WiseOwlEnglish_Backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "test_answer",
        indexes = {
                @Index(name = "idx_tans_attempt", columnList = "attempt_id"),
                @Index(name = "idx_tans_question", columnList = "question_id"),
                @Index(name = "idx_tans_option", columnList = "option_id"),
                @Index(name = "idx_tans_option_left", columnList = "option_left_id"),
                @Index(name = "idx_tans_option_right", columnList = "option_right_id")
        }
)
public class TestAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id")
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private TestQuestion question;

    // Chọn 1 / Đúng–Sai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private TestOption option;

    // Matching (nối trái-phải)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_left_id")
    private TestOption optionLeft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_right_id")
    private TestOption optionRight;

    // Ordering (lưu thứ tự người làm) – JSON string
    @Column(name = "sequence_json", columnDefinition = "text")
    private String sequenceJson;

    // Fill-in-blank / Numeric
    @Column(name = "text_input", columnDefinition = "text")
    private String textInput;

    @Column(name = "numeric_input")
    private String numericInput; // nếu cần số thực, có thể dùng BigDecimal

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}