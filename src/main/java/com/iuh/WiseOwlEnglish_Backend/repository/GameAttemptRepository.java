package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.AttemptStatus;
import com.iuh.WiseOwlEnglish_Backend.model.GameAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameAttemptRepository extends JpaRepository<GameAttempt, Long> {
    /**
     * Tìm một lần chơi (attempt) dựa trên người chơi và game.
     */
    Optional<GameAttempt> findByLearnerProfile_IdAndGame_IdAndStatus(
            Long learnerProfileId,
            Long gameId,
            AttemptStatus status
    );

}
