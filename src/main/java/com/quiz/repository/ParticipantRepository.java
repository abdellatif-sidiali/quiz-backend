package com.quiz.repository;

import com.quiz.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findBySessionId(Long sessionId);

    @Query("SELECT p FROM Participant p WHERE p.session.id = :sessionId ORDER BY p.totalScore DESC, p.totalResponseTime ASC")
    List<Participant> findBySessionIdOrderByScoreDesc(Long sessionId);

    Optional<Participant> findBySocketId(String socketId);

    boolean existsBySessionIdAndFirstNameAndLastName(Long sessionId, String firstName, String lastName);
}
