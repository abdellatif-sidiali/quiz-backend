package com.quiz.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinSessionRequest {
    private String sessionCode;
    private String firstName;
    private String lastName;
}
