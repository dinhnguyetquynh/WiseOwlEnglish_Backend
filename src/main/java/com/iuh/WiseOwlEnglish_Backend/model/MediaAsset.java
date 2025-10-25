package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iuh.WiseOwlEnglish_Backend.enums.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "media_asset",
        indexes = {
                @Index(name = "idx_media_vocab", columnList = "vocabulary_id"),
                @Index(name = "idx_media_sentence", columnList = "sentence_id")
        }
)
public class MediaAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "storage_provider")
    private String storageProvider;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "tag")
    private String tag;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    @JsonIgnore
    private Vocabulary vocabulary; // set khi media thuộc về 1 từ vựng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sentence_id")
    @JsonIgnore
    private Sentence sentence;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_question_id", foreignKey = @ForeignKey(name = "fk_media_asset_test_question"),unique = true)
    @JsonIgnore
    private TestQuestion testQuestion;

}
