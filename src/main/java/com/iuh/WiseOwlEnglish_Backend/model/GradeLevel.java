package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "grade_levels")
public class GradeLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade_name", nullable = false, length = 50)
    private String gradeName;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;   // 1..5 để sắp xếp thứ tự

    @OneToMany(mappedBy = "gradeLevel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Lesson> lessons;

    @OneToMany(mappedBy = "gradeLevel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<LearnerGradeProgress> learnerGradeProgresses;



}
