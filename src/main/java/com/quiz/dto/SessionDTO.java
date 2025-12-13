package com.quiz.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private Long id;
    private String code;
    private String status;
    private Integer currentQuestionIndex;
    private Boolean openRegistration;
    private Integer maxParticipants;
    private Boolean showImmediateFeedback;
    private Long quizId;
    private String quizTitle;
    private Integer totalQuestions;
    private Integer participantCount;
}
