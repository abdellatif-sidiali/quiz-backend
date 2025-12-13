package com.quiz.service;

import com.quiz.model.*;
import com.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private static final int MAX_TIME_BONUS = 5;

    private final ResponseRepository responseRepository;
    private final ParticipantRepository participantRepository;

    /**
     * Calculate score for an answer
     * Points = basePoints + timeBonus (if correct)
     * timeBonus = (remainingTime / totalTime) * MAX_TIME_BONUS
     */
    public int calculateScore(Question question, Answer answer, long responseTimeMs) {
        if (!answer.getIsCorrect()) {
            return 0;
        }

        int basePoints = question.getPoints();
        int timeLimitMs = question.getEffectiveTimeLimit() * 1000;

        // Calculate time bonus based on how quickly they answered
        long remainingMs = Math.max(0, timeLimitMs - responseTimeMs);
        double timeBonusRatio = (double) remainingMs / timeLimitMs;
        int timeBonus = (int) Math.round(timeBonusRatio * MAX_TIME_BONUS);

        return basePoints + timeBonus;
    }

    /**
     * Record a participant's response and update their score
     */
    public ParticipantResponse recordResponse(Participant participant, Question question,
            Answer answer, long responseTimeMs) {
        // Check if already answered
        if (responseRepository.existsByParticipantIdAndQuestionId(participant.getId(), question.getId())) {
            throw new RuntimeException("Already answered this question");
        }

        int points = calculateScore(question, answer, responseTimeMs);

        ParticipantResponse response = new ParticipantResponse();
        response.setParticipant(participant);
        response.setQuestion(question);
        response.setAnswer(answer);
        response.setResponseTimeMs(responseTimeMs);
        response.setPointsEarned(points);
        response.setIsCorrect(answer.getIsCorrect());

        responseRepository.save(response);

        // Update participant's total score and time
        participant.setTotalScore(participant.getTotalScore() + points);
        if (answer.getIsCorrect()) {
            participant.setTotalResponseTime(participant.getTotalResponseTime() + responseTimeMs);
        }
        participantRepository.save(participant);

        return response;
    }
}
