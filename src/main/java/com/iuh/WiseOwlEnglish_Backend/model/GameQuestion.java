package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iuh.WiseOwlEnglish_Backend.enums.PromptType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "game_question",
        indexes = {
                @Index(name = "idx_gq_game", columnList = "game_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_gq_game_pos", columnNames = {"game_id", "position"})
        }
)
public class GameQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private Integer position;               // thứ tự trong game

    // ---- Prompt (chỉ 1 prompt/câu) ----
    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = true, length = 16)
    private PromptType promptType;          // VOCAB/SENTENCE/IMAGE/AUDIO

    @Column(name = "prompt_ref_id", nullable = true)
    private Long promptRefId;               // id của vocab/sentence/media

    // Cho FILL_IN_BLANK (tùy game type)
    @Column(name = "hidden_word")
    private String hiddenWord;

    @OneToMany(mappedBy = "gameQuestion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GameOption> options;

    @Column(name = "reward_core", nullable = false)
    private int rewardCore;


    @Column(name = "question_text")
    private String questionText;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

}
