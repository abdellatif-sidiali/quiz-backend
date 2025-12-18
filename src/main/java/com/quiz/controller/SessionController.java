package com.quiz.controller;

import com.quiz.dto.*;
import com.quiz.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;
    
    private static final String SESSION_EXCHANGE = "/exchange/amq.topic/session.";

    @PostMapping("/create/{quizId}")
    public ResponseEntity<SessionDTO> createSession(@PathVariable Long quizId) {
        return ResponseEntity.ok(sessionService.createSession(quizId));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<SessionDTO> getSessionByCode(@PathVariable String code) {
        return ResponseEntity.ok(sessionService.getSessionByCode(code));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SessionDTO>> getActiveSessions() {
        return ResponseEntity.ok(sessionService.getActiveSessions());
    }

    @PostMapping("/join")
    public ResponseEntity<ParticipantDTO> joinSession(@RequestBody JoinSessionRequest request) {
        ParticipantDTO participant = sessionService.joinSession(request);

        // Broadcast to session that a new participant joined
        SessionDTO session = sessionService.getSessionByCode(request.getSessionCode());
        Map<String, Object> message = new HashMap<>();
        message.put("type", "PARTICIPANT_JOINED");
        message.put("participant", participant);
        message.put("participantCount", session.getParticipantCount());
        //messagingTemplate.convertAndSend("/topic/session/" + request.getSessionCode().toUpperCase(), message);
        messagingTemplate.convertAndSend(
                SESSION_EXCHANGE + request.getSessionCode().toUpperCase(),
                message
        );

        return ResponseEntity.ok(participant);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<SessionDTO> startSession(@PathVariable Long id) {
        SessionDTO session = sessionService.startSession(id);

        // Broadcast session started
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SESSION_STARTED");
        message.put("status", session.getStatus());
        //messagingTemplate.convertAndSend("/topic/session/" + session.getCode(), message);
        messagingTemplate.convertAndSend(SESSION_EXCHANGE  + session.getCode(), message);

        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/question/{index}")
    public ResponseEntity<QuestionDTO> showQuestion(@PathVariable Long id, @PathVariable int index) {
        QuestionDTO question = sessionService.showQuestion(id, index);
        SessionDTO session = sessionService.getSessionById(id);

        // Broadcast question to all participants
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_QUESTION");
        message.put("question", question);
        message.put("questionIndex", index);
        message.put("totalQuestions", session.getTotalQuestions());
        //messagingTemplate.convertAndSend("/topic/session/" + session.getCode(), message);
        messagingTemplate.convertAndSend(SESSION_EXCHANGE + session.getCode(), message);

        return ResponseEntity.ok(question);
    }

    @PostMapping("/{id}/end-question")
    public ResponseEntity<SessionDTO> endQuestion(@PathVariable Long id) {
        SessionDTO session = sessionService.endQuestion(id);

        // Broadcast question ended
        Map<String, Object> message = new HashMap<>();
        message.put("type", "QUESTION_ENDED");
        message.put("status", session.getStatus());
        //messagingTemplate.convertAndSend("/topic/session/" + session.getCode(), message);
        messagingTemplate.convertAndSend(SESSION_EXCHANGE + session.getCode(), message);

        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<SessionDTO> finishSession(@PathVariable Long id) {
        SessionDTO session = sessionService.finishSession(id);
        ScoreboardDTO scoreboard = sessionService.getScoreboard(id);

        // Broadcast session ended with final scoreboard
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SESSION_ENDED");
        message.put("scoreboard", scoreboard);
        //messagingTemplate.convertAndSend("/topic/session/" + session.getCode(), message);
        messagingTemplate.convertAndSend(SESSION_EXCHANGE + session.getCode(), message);

        return ResponseEntity.ok(session);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantDTO>> getParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getParticipants(id));
    }

    @GetMapping("/{id}/scoreboard")
    public ResponseEntity<ScoreboardDTO> getScoreboard(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getScoreboard(id));
    }
}
