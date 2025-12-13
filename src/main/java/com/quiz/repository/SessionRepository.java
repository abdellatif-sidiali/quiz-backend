package com.quiz.repository;

import com.quiz.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findByCode(String code);

    boolean existsByCode(String code);
}
