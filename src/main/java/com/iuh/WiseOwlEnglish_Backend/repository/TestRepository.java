package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
//    Optional<Test> findByLessonTest_Id(Long lessonId);
    List<Test> findByLessonTest_Id(Long lessonId);

}
