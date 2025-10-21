package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sentences", indexes = {
        @Index(name = "idx_sentence_lesson", columnList = "lesson_id")},
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_sentence_lesson_order", columnNames = {"lesson_id", "order_index"})
        })
public class Sentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int orderIndex;   // 1..n để sắp xếp thứ tự
    private String sentence_en;
    private String sentence_vi;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @OneToMany(
            mappedBy = "sentence",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @JsonIgnore
    private Lesson lessonSentence;
}
