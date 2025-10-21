package com.iuh.WiseOwlEnglish_Backend.model;

import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lesson_progress",
        indexes = {
                @Index(name = "idx_lpr_lesson", columnList = "lesson_id"),
                @Index(name = "idx_lpr_learner", columnList = "learner_profile_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_lpr_learner_lesson", columnNames = {"learner_profile_id", "lesson_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress {
    @Id
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_profile_id", nullable = false)
    private LearnerProfile learnerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_item_type")
    private ItemType lastItemType;

    @Column(name = "last_item_ref_id")
    private int lastItemRefId;
    @Column(name = "last_item_index")
    private int lastItemIndex;


    @Column(name = "percent_complete")
    private double percentComplete;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //add status for lesson progress(active, completed, not started)

}
