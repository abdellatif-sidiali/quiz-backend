package com.quiz.repository;

import com.quiz.model.ParticipantResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseRepository extends JpaRepository<ParticipantResponse, Long> {
    List<ParticipantResponse> findByParticipantId(Long participantId);

    Optional<ParticipantResponse> findByParticipantIdAndQuestionId(Long participantId, Long questionId);

    boolean existsByParticipantIdAndQuestionId(Long participantId, Long questionId);

    List<ParticipantResponse> findByQuestionId(Long questionId);
}
