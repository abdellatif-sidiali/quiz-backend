package com.quiz.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private Integer timePerQuestion;
    private Boolean shuffleQuestions;
    private Boolean shuffleAnswers;
    private Boolean liveScoreboard;
    private List<QuestionDTO> questions;
}
