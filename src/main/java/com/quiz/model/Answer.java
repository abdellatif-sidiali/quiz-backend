package com.quiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "answers")
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(nullable = false, length = 1)
    @Getter
    @Setter
    private String label; // A, B, C, D

    @Column(nullable = false, length = 200)
    @Getter
    @Setter
    private String text;

    @Column(name = "is_correct")
    private Boolean correct = false;

    @JsonProperty("isCorrect")
    public Boolean getIsCorrect() {
        return this.correct;
    }

    @JsonProperty("isCorrect")
    public void setIsCorrect(Boolean isCorrect) {
        this.correct = isCorrect;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    @Getter
    @Setter
    private Question question;
}
