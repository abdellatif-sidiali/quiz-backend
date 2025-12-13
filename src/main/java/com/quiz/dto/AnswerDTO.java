package com.quiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnswerDTO {
    private Long id;
    private String label;
    private String text;
    private Boolean correct;

    public AnswerDTO() {
    }

    public AnswerDTO(Long id, String label, String text, Boolean isCorrect) {
        this.id = id;
        this.label = label;
        this.text = text;
        this.correct = isCorrect;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("isCorrect")
    public Boolean getIsCorrect() {
        return correct;
    }

    @JsonProperty("isCorrect")
    public void setIsCorrect(Boolean isCorrect) {
        this.correct = isCorrect;
    }
}
