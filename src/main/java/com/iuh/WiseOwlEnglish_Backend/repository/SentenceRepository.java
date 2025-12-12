package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.Sentence;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findByLessonSentenceIdOrderByOrderIndexAsc(Long lessonId);
    Optional<Sentence> findById(Long id);

    List<Sentence> findByLessonSentence_IdAndIsForLearning(Long lessonId, boolean isForLearning);

    long countByLessonSentence_IdAndIsForLearning(Long lessonId, boolean isForLearning);

    List<Sentence> findByLessonSentence_Id(long lessonId);

    List<Sentence> findByLessonSentenceIdAndDeletedAtIsNullOrderByOrderIndexAsc(long lessonId);

    @Query("SELECT COALESCE(MAX(s.orderIndex), 0) FROM Sentence s WHERE s.lessonSentence.id = :lessonId")
    int findMaxOrderIndexByLessonId(@Param("lessonId") Long lessonId);

    long countByDeletedAtIsNull();
}
