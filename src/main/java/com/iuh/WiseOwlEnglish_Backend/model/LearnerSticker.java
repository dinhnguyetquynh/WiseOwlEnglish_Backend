package com.iuh.WiseOwlEnglish_Backend.model;

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
@Table(name = "learner_stickers")
public class LearnerSticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id")
    private LearnerProfile learnerProfile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sticker_id")
    private Sticker sticker;

    private LocalDateTime purchasedAt;
}
