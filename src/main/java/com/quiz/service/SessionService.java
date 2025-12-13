package com.quiz.service;

import com.quiz.dto.*;
import com.quiz.model.*;
import com.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final QuizRepository quizRepository;
    private final ParticipantRepository participantRepository;
    private final QuestionRepository questionRepository;

    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public SessionDTO createSession(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        GameSession session = new GameSession();
        session.setCode(generateUniqueCode());
        session.setQuiz(quiz);
        session.setStatus(GameSession.SessionStatus.WAITING);
        session.setCurrentQuestionIndex(-1);
        session.setCreatedAt(LocalDateTime.now());

        GameSession saved = sessionRepository.save(session);
        return toDTO(saved);
    }

    public SessionDTO getSessionByCode(String code) {
        GameSession session = sessionRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Session not found with code: " + code));
        return toDTO(session);
    }

    public SessionDTO getSessionById(Long id) {
        GameSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
        return toDTO(session);
    }

    public List<SessionDTO> getActiveSessions() {
        return sessionRepository.findAll().stream()
                .filter(s -> s.getStatus() != GameSession.SessionStatus.FINISHED)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipantDTO joinSession(JoinSessionRequest request) {
        GameSession session = sessionRepository.findByCode(request.getSessionCode().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getOpenRegistration()) {
            throw new RuntimeException("Registration is closed for this session");
        }

        if (session.getStatus() == GameSession.SessionStatus.FINISHED) {
            throw new RuntimeException("Session has already ended");
        }

        // Check max participants
        if (session.getMaxParticipants() != null &&
                session.getParticipants().size() >= session.getMaxParticipants()) {
            throw new RuntimeException("Session is full");
        }

        // Check for duplicate name
        boolean exists = participantRepository.existsBySessionIdAndFirstNameAndLastName(
                session.getId(), request.getFirstName(), request.getLastName());
        if (exists) {
            throw new RuntimeException("A participant with this name already exists");
        }

        Participant participant = new Participant();
        participant.setFirstName(request.getFirstName().trim());
        participant.setLastName(request.getLastName().trim());
        participant.setSession(session);
        participant.setTotalScore(0);
        participant.setTotalResponseTime(0L);
        participant.setIsConnected(true);

        Participant saved = participantRepository.save(participant);
        return toParticipantDTO(saved, 0);
    }

    @Transactional
    public SessionDTO startSession(Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setStatus(GameSession.SessionStatus.IN_PROGRESS);
        session.setOpenRegistration(false);

        return toDTO(sessionRepository.save(session));
    }

    @Transactional
    public QuestionDTO showQuestion(Long sessionId, int questionIndex) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Question> questions = questionRepository.findByQuizIdOrderByQuestionOrderAsc(
                session.getQuiz().getId());

        if (session.getQuiz().getShuffleQuestions() && session.getCurrentQuestionIndex() == -1) {
            // Shuffle on first question
            Collections.shuffle(questions);
        }

        if (questionIndex < 0 || questionIndex >= questions.size()) {
            throw new RuntimeException("Invalid question index");
        }

        session.setCurrentQuestionIndex(questionIndex);
        session.setStatus(GameSession.SessionStatus.QUESTION_ACTIVE);
        session.setQuestionStartTime(LocalDateTime.now());
        sessionRepository.save(session);

        Question question = questions.get(questionIndex);
        return toQuestionDTOForParticipant(question, session.getQuiz().getShuffleAnswers());
    }

    @Transactional
    public SessionDTO endQuestion(Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setStatus(GameSession.SessionStatus.SHOWING_RESULTS);
        return toDTO(sessionRepository.save(session));
    }

    @Transactional
    public SessionDTO finishSession(Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setStatus(GameSession.SessionStatus.FINISHED);
        return toDTO(sessionRepository.save(session));
    }

    public List<ParticipantDTO> getParticipants(Long sessionId) {
        List<Participant> participants = participantRepository.findBySessionIdOrderByScoreDesc(sessionId);
        return toRankedParticipantDTOs(participants);
    }

    public ScoreboardDTO getScoreboard(Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Participant> participants = participantRepository.findBySessionIdOrderByScoreDesc(sessionId);
        List<ParticipantDTO> rankings = toRankedParticipantDTOs(participants);

        int totalQuestions = session.getQuiz().getQuestions().size();
        int answered = session.getCurrentQuestionIndex() + 1;

        ScoreboardDTO scoreboard = new ScoreboardDTO();
        scoreboard.setRankings(rankings);
        scoreboard.setTotalParticipants(participants.size());
        scoreboard.setTotalQuestions(totalQuestions);
        scoreboard.setQuestionsAnswered(Math.max(0, answered));

        return scoreboard;
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
            }
            code = sb.toString();
        } while (sessionRepository.existsByCode(code));

        return code;
    }

    public SessionDTO toDTO(GameSession session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setCode(session.getCode());
        dto.setStatus(session.getStatus().name());
        dto.setCurrentQuestionIndex(session.getCurrentQuestionIndex());
        dto.setOpenRegistration(session.getOpenRegistration());
        dto.setMaxParticipants(session.getMaxParticipants());
        dto.setShowImmediateFeedback(session.getShowImmediateFeedback());
        dto.setQuizId(session.getQuiz().getId());
        dto.setQuizTitle(session.getQuiz().getTitle());
        dto.setTotalQuestions(session.getQuiz().getQuestions().size());
        dto.setParticipantCount(session.getParticipants().size());
        return dto;
    }

    public ParticipantDTO toParticipantDTO(Participant participant, int rank) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setId(participant.getId());
        dto.setFirstName(participant.getFirstName());
        dto.setLastName(participant.getLastName());
        dto.setTotalScore(participant.getTotalScore());
        dto.setRank(rank);
        dto.setIsConnected(participant.getIsConnected());
        return dto;
    }

    private List<ParticipantDTO> toRankedParticipantDTOs(List<Participant> participants) {
        return participants.stream()
                .map(p -> toParticipantDTO(p, participants.indexOf(p) + 1))
                .collect(Collectors.toList());
    }

    private QuestionDTO toQuestionDTOForParticipant(Question question, boolean shuffleAnswers) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setText(question.getText());
        dto.setTimeLimit(question.getEffectiveTimeLimit());
        dto.setMediaUrl(question.getMediaUrl());
        dto.setQuestionOrder(question.getQuestionOrder());

        List<Answer> answers = question.getAnswers();
        if (shuffleAnswers) {
            answers = new java.util.ArrayList<>(answers);
            Collections.shuffle(answers);
        }

        // Don't send isCorrect to participants
        dto.setAnswers(answers.stream()
                .map(a -> {
                    AnswerDTO aDto = new AnswerDTO();
                    aDto.setId(a.getId());
                    aDto.setLabel(a.getLabel());
                    aDto.setText(a.getText());
                    aDto.setIsCorrect(null); // Hide correct answer
                    return aDto;
                })
                .collect(Collectors.toList()));

        return dto;
    }
}
