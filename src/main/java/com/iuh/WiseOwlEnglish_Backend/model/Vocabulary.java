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
@Table(name = "vocabularies", indexes = {
        @Index(name = "idx_vocab_lesson", columnList = "lesson_id")},
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_vocab_lesson_order", columnNames = {"lesson_id", "order_index"})
        })
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String term_en;
    private String term_vi;
    private String phonetic;
    @Column(name = "order_index")
    private int orderIndex;   // 1..n để sắp xếp thứ tự

    @Column(name = "part_of_speech")
    private String partOfSpeech;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(
            mappedBy = "vocabulary",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @JsonIgnore
    private Lesson lessonVocabulary;
}
