package com.iuh.WiseOwlEnglish_Backend.model;

import com.iuh.WiseOwlEnglish_Backend.enums.TestType;
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
        name = "test",
        indexes = {
                @Index(name = "idx_test_lesson", columnList = "lesson_id")
        }
)
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lesson_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_test_lesson")
    )
    private Lesson lessonTest;

    private String title;


    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", length = 32, nullable = false)
    private TestType testType;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "duration_min")
    private Integer durationMin;      // thời gian làm bài (phút)

    @Column(name = "is_active")
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestQuestion> questions;
}