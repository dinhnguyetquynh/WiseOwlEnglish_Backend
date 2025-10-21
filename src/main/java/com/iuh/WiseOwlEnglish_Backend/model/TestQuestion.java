package com.iuh.WiseOwlEnglish_Backend.model;

import com.iuh.WiseOwlEnglish_Backend.enums.StemType;
import com.iuh.WiseOwlEnglish_Backend.enums.TestQuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "test_question",
        indexes = {
                @Index(name = "idx_tq_test", columnList = "test_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tq_test_order", columnNames = {"test_id", "order_in_test"})
        }
)
public class TestQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "test_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_test_question_test")
    )
    private Test test;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 24)
    private TestQuestionType questionType;

    // ---- Prompt (stem) chỉ 1 loại/câu ----
    @Enumerated(EnumType.STRING)
    @Column(name = "stem_type", nullable = false, length = 16)
    private StemType stemType;          // TEXT/VOCAB/SENTENCE/IMAGE/AUDIO

    @Column(name = "stem_ref_id")
    private Long stemRefId;             // id của entity tương ứng (nếu không phải TEXT)

    @Column(name = "stem_text", columnDefinition = "text")
    private String stemText;            // dùng khi stemType = TEXT

    private Integer difficulty;         // 1..5
    @Column(name = "max_score")
    private Integer maxScore;           // điểm tối đa cho câu
    @Column(name = "order_in_test")
    private Integer orderInTest;        // thứ tự trong bài

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestOption> options;
}
