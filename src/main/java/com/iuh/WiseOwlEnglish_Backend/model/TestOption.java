package com.iuh.WiseOwlEnglish_Backend.model;

import com.iuh.WiseOwlEnglish_Backend.enums.ContentType;
import com.iuh.WiseOwlEnglish_Backend.enums.Side;
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
        name = "test_option",
        indexes = {
                @Index(name = "idx_to_question", columnList = "question_id")
        }
)
public class TestOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "question_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_test_option_question")
    )
    private TestQuestion question;

    // Nội dung option (đa hình)
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = true, length = 16)
    private ContentType contentType;     // TEXT/VOCAB/SENTENCE/IMAGE/AUDIO

    @Column(name = "content_ref_id")
    private Long contentRefId;           // id của entity tương ứng (nếu không phải TEXT)

    @Column(columnDefinition = "text")
    private String text;                 // khi contentType = TEXT (có thể trống)

    @Column(name = "is_correct", nullable = false)
    private boolean correct;             // cho trắc nghiệm/true-false

    @Column(name = "order_in_question")
    private Integer order;               // thứ tự hiển thị

    // Dành cho matching
    @Enumerated(EnumType.STRING)
    @Column(length = 8, nullable = true)
    private Side side;                   // LEFT/RIGHT

    @Column(name = "pair_key")
    private String pairKey;              // mã ghép cặp

    // Dành cho ORDERING (xếp thứ tự đúng)
    @Column(name = "correct_order")
    private Integer correctOrder;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
