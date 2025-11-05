package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.GameAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameAnswerRepository extends JpaRepository<GameAnswer,Long> {
}
