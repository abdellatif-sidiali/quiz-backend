package com.quiz.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSession {

    public enum SessionStatus {
        WAITING, // Waiting for participants
        IN_PROGRESS, // Quiz is running
        QUESTION_ACTIVE, // A question is currently being answered
        SHOWING_RESULTS, // Showing results of a question
        FINISHED // Quiz completed
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.WAITING;

    @Column(name = "current_question_index")
    private Integer currentQuestionIndex = -1;

    @Column(name = "question_start_time")
    private LocalDateTime questionStartTime;

    @Column(name = "open_registration")
    private Boolean openRegistration = true;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "show_immediate_feedback")
    private Boolean showImmediateFeedback = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    private Quiz quiz;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();
}
