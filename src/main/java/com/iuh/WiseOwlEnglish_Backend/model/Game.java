package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(
        name = "game",
        indexes = {
                @Index(name = "idx_game_lesson", columnList = "lesson_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GameType type;

    private Integer difficulty;     // 1..5

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @JsonIgnore
    private Lesson lesson;


    // hiệu ứng âm thanh chung (nếu có) → FK tới MediaAsset.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correct_audio_id")
    private MediaAsset correctAudio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrong_audio_id")
    private MediaAsset wrongAudio;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameQuestion> questions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private boolean active ;

}
