package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemStatus;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "content_progress",
        indexes = {
                @Index(name = "idx_cpr_lesson", columnList = "lesson_id"),
                @Index(name = "idx_cpr_learner", columnList = "learner_profile_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_cpr_learner_lesson_item",
                        columnNames = {"learner_profile_id", "lesson_id", "item_type", "item_ref_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentProgress {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_profile_id", nullable = false)
    @JsonIgnore
    private LearnerProfile learnerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @JsonIgnore
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "item_ref_id", nullable = false)
    private int itemRefId;
    @Column(name = "item_index", nullable = false)
    private int itemIndex; // Index of the item within its type (e.g., 1st vocab, 2nd sentence, etc.)

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime updatedAt;


}
