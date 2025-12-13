package com.quiz.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String text;
    private Integer points;
    private Integer timeLimit;
    private String mediaUrl;
    private Integer questionOrder;
    private List<AnswerDTO> answers;
}
