package com.quiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "participant_responses")
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(name = "response_time_ms")
    @Getter
    @Setter
    private Long responseTimeMs; // Time taken to answer in milliseconds

    @Column(name = "points_earned")
    @Getter
    @Setter
    private Integer pointsEarned = 0;

    @Column(name = "is_correct")
    private Boolean correct = false;

    public Boolean getIsCorrect() {
        return this.correct;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.correct = isCorrect;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    @Getter
    @Setter
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @Getter
    @Setter
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    @Getter
    @Setter
    private Answer answer;
}
