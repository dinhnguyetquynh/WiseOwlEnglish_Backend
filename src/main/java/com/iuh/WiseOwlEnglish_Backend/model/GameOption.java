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
@Entity
@Table(
        name = "game_option",
        indexes = {
                @Index(name = "idx_go_question", columnList = "question_id")
        }
)
public class GameOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "question_id", nullable = false)
    private GameQuestion gameQuestion;

    // ---- Nội dung option (đa hình) ----
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = true, length = 16)
    private ContentType contentType;      // VOCAB/SENTENCE/IMAGE/AUDIO

    @Column(name = "content_ref_id", nullable = true)
    private Long contentRefId;            // id của vocab/sentence/media

    @Column(name = "is_correct", nullable = false)
    private boolean correct;              // dùng cho MULTI_CHOICE/TRUE_FALSE

    // Dành cho matching
    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private Side side;                    // LEFT/RIGHT

    @Column(name = "pair_key")
    private String pairKey;               // mã ghép cặp

    private Integer position;             // thứ tự hiển thị

    @Column(name = "answer_text")
    private String answerText;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
