package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.Sentence;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findByLessonSentenceIdOrderByOrderIndexAsc(Long lessonId);
    Optional<Sentence> findById(Long id);

    List<Sentence> findByLessonSentence_IdAndIsForLearning(Long lessonId, boolean isForLearning);
}
