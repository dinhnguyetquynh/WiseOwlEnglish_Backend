package com.iuh.WiseOwlEnglish_Backend.model;

import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
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
        name = "incorrect_item_log",
        indexes = {
                // Tối ưu hóa truy vấn đếm lỗi
                @Index(name = "idx_incorrect_learner_lesson", columnList = "learner_profile_id, lesson_id, item_type")
        }
)
public class IncorrectItemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_profile_id")
    private LearnerProfile learnerProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType; // VOCAB hoặc SENTENCE

    @Column(name = "item_ref_id", nullable = false)
    private Long itemRefId; // ID của Vocabulary hoặc Sentence

    @Column(name = "wrong_at", nullable = false)
    private LocalDateTime wrongAt;
}
