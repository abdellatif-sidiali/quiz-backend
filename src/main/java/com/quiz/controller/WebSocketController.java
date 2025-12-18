package com.quiz.controller;

import com.quiz.dto.*;
import com.quiz.model.*;
import com.quiz.repository.*;
import com.quiz.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionService sessionService;
    private final ScoringService scoringService;
    private final ParticipantRepository participantRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ResponseRepository responseRepository;

    // ✅ IMPORTANT : Utiliser l'exchange pour toutes les destinations
    private static final String SESSION_EXCHANGE = "/exchange/amq.topic/session.";

    /**
     * Handle answer submission from participant
     */
    @MessageMapping("/session/{sessionCode}/answer")
    public void submitAnswer(@DestinationVariable String sessionCode,
            @Payload SubmitAnswerRequest request) {
        log.info("Received answer from participant {} for question {} with answer {}",
                request.getParticipantId(), request.getQuestionId(), request.getAnswerId());

        try {
            Participant participant = participantRepository.findById(request.getParticipantId())
                    .orElseThrow(() -> new RuntimeException("Participant not found"));

            Question question = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            Answer answer = answerRepository.findById(request.getAnswerId())
                    .orElseThrow(() -> new RuntimeException("Answer not found"));

            log.info("Answer isCorrect: {}", answer.getIsCorrect());

            // Check if already answered
            if (responseRepository.existsByParticipantIdAndQuestionId(
                    participant.getId(), question.getId())) {
                log.warn("Participant {} already answered question {}", participant.getId(), question.getId());
                sendErrorToParticipant(sessionCode, participant.getId(), "Already answered this question");
                return;
            }

            // Record and score the response
            ParticipantResponse response = scoringService.recordResponse(
                    participant, question, answer, request.getResponseTimeMs());

            // Re-fetch participant to get updated score
            Participant updatedParticipant = participantRepository.findById(request.getParticipantId())
                    .orElse(participant);

            log.info("Response recorded: isCorrect={}, pointsEarned={}, totalScore={}",
                    response.getIsCorrect(), response.getPointsEarned(), updatedParticipant.getTotalScore());

            // ✅ CORRECTION: Send feedback using exchange routing key
            Map<String, Object> feedback = new HashMap<>();
            feedback.put("type", "ANSWER_RESULT");
            feedback.put("questionId", question.getId());
            feedback.put("isCorrect", response.getIsCorrect());
            feedback.put("pointsEarned", response.getPointsEarned());
            feedback.put("totalScore", updatedParticipant.getTotalScore());

            String destination = SESSION_EXCHANGE + sessionCode + ".participant." + participant.getId();
            log.info("Sending feedback to: {}", destination);
            messagingTemplate.convertAndSend(destination, feedback);

            // Notify admin of response count update
            long responseCount = responseRepository.findByQuestionId(question.getId()).size();
            Map<String, Object> adminUpdate = new HashMap<>();
            adminUpdate.put("type", "RESPONSE_COUNT");
            adminUpdate.put("questionId", question.getId());
            adminUpdate.put("count", responseCount);

            // ✅ CORRECTION: Send to admin using exchange
            messagingTemplate.convertAndSend(SESSION_EXCHANGE + sessionCode + ".admin", adminUpdate);

        } catch (Exception e) {
            log.error("Error processing answer: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcast a question to all participants in a session
     */
    public void broadcastQuestion(String sessionCode, QuestionDTO question) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_QUESTION");
        message.put("question", question);

        messagingTemplate.convertAndSend(SESSION_EXCHANGE + sessionCode, message);
        log.info("Broadcasted question {} to session {}", question.getId(), sessionCode);
    }

    /**
     * Broadcast end of question (time up)
     */
    public void broadcastQuestionEnd(String sessionCode, Long questionId, AnswerDTO correctAnswer) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "QUESTION_ENDED");
        message.put("questionId", questionId);
        message.put("correctAnswer", correctAnswer);

        messagingTemplate.convertAndSend(SESSION_EXCHANGE + sessionCode, message);
    }

    /**
     * Broadcast scoreboard update
     */
    public void broadcastScoreboard(String sessionCode, ScoreboardDTO scoreboard) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SCOREBOARD_UPDATE");
        message.put("scoreboard", scoreboard);

        messagingTemplate.convertAndSend(SESSION_EXCHANGE + sessionCode, message);
    }

    /**
     * Broadcast participant joined
     */
    public void broadcastParticipantJoined(String sessionCode, ParticipantDTO participant) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "PARTICIPANT_JOINED");
        message.put("participant", participant);

        messagingTemplate.convertAndSend(SESSION_EXCHANGE + sessionCode, message);
    }

    /**
     * Broadcast session status change
     */
    public void broadcastSessionStatus(String sessionCode, String status) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SESSION_STATUS");
        message.put("status", status);

        messagingTemplate.convertAndSend(SESSION_EXCHANGE + sessionCode, message);
    }

    /**
     * Broadcast session ended
     */
    public void broadcastSessionEnd(String sessionCode, ScoreboardDTO finalScoreboard) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SESSION_ENDED");
        message.put("scoreboard", finalScoreboard);

        messagingTemplate.convertAndSend(SESSION_EXCHANGE + sessionCode, message);
    }

    private void sendErrorToParticipant(String sessionCode, Long participantId, String error) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ERROR");
        message.put("message", error);

        // ✅ CORRECTION: Send error using exchange
        messagingTemplate.convertAndSend(
            SESSION_EXCHANGE + sessionCode + ".participant." + participantId, 
            message
        );
        log.warn("Error for participant {}: {}", participantId, error);
    }
}