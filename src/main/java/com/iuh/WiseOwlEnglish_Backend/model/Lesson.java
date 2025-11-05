package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lessons",
        indexes = {@Index(name = "idx_lesson_grade", columnList = "grade_level_id")},
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_lesson_grade_order", columnNames = {"grade_level_id", "order_index"})
        }
)
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "unit_name", nullable = false)
    private String unitName;
    @Column(name = "lesson_name", nullable = false)
    private String lessonName;
    @Column(name = "order_index", nullable = false)
    private int orderIndex;
    private boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_level_id", nullable = false)
    private GradeLevel gradeLevel;

    @OneToMany(mappedBy = "lessonVocabulary", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Vocabulary> vocabularies;


    @OneToMany(mappedBy = "lessonSentence", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Sentence> sentences;

    @OneToMany(mappedBy = "lessonTest", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Test> tests;

    private String mascot;

}
