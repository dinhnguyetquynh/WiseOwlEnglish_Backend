package com.iuh.WiseOwlEnglish_Backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category_stickers")
public class CategorySticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false)
    private String categoryName; // VD: Animals, Fruit...

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Sticker> stickers;
}
