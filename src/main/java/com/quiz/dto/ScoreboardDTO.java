package com.quiz.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreboardDTO {
    private List<ParticipantDTO> rankings;
    private Integer totalParticipants;
    private Integer totalQuestions;
    private Integer questionsAnswered;
}
