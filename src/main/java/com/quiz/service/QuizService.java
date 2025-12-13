package com.quiz.service;

import com.quiz.dto.*;
import com.quiz.model.*;
import com.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public QuizDTO getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        return toDTO(quiz);
    }

    @Transactional
    public QuizDTO createQuiz(QuizDTO dto) {
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTimePerQuestion(dto.getTimePerQuestion() != null ? dto.getTimePerQuestion() : 30);
        quiz.setShuffleQuestions(dto.getShuffleQuestions() != null ? dto.getShuffleQuestions() : false);
        quiz.setShuffleAnswers(dto.getShuffleAnswers() != null ? dto.getShuffleAnswers() : false);
        quiz.setLiveScoreboard(dto.getLiveScoreboard() != null ? dto.getLiveScoreboard() : false);

        Quiz savedQuiz = quizRepository.save(quiz);

        if (dto.getQuestions() != null) {
            for (int i = 0; i < dto.getQuestions().size(); i++) {
                QuestionDTO qDto = dto.getQuestions().get(i);
                addQuestionToQuiz(savedQuiz, qDto, i);
            }
        }

        return toDTO(quizRepository.findById(savedQuiz.getId()).get());
    }

    @Transactional
    public QuizDTO updateQuiz(Long id, QuizDTO dto) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));

        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTimePerQuestion(dto.getTimePerQuestion());
        quiz.setShuffleQuestions(dto.getShuffleQuestions());
        quiz.setShuffleAnswers(dto.getShuffleAnswers());
        quiz.setLiveScoreboard(dto.getLiveScoreboard());

        quizRepository.save(quiz);
        return toDTO(quiz);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }

    @Transactional
    public QuestionDTO addQuestion(Long quizId, QuestionDTO dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        int order = quiz.getQuestions().size();
        Question question = addQuestionToQuiz(quiz, dto, order);
        return toQuestionDTO(question);
    }

    @Transactional
    public QuestionDTO updateQuestion(Long questionId, QuestionDTO dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        question.setText(dto.getText());
        question.setPoints(dto.getPoints());
        question.setTimeLimit(dto.getTimeLimit());
        question.setMediaUrl(dto.getMediaUrl());

        // Update answers
        if (dto.getAnswers() != null) {
            question.getAnswers().clear();
            for (AnswerDTO aDto : dto.getAnswers()) {
                Answer answer = new Answer();
                answer.setLabel(aDto.getLabel());
                answer.setText(aDto.getText());
                answer.setIsCorrect(aDto.getIsCorrect());
                answer.setQuestion(question);
                question.getAnswers().add(answer);
            }
        }

        questionRepository.save(question);
        return toQuestionDTO(question);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    private Question addQuestionToQuiz(Quiz quiz, QuestionDTO dto, int order) {
        Question question = new Question();
        question.setText(dto.getText());
        question.setPoints(dto.getPoints() != null ? dto.getPoints() : 1);
        question.setTimeLimit(dto.getTimeLimit());
        question.setMediaUrl(dto.getMediaUrl());
        question.setQuestionOrder(order);
        question.setQuiz(quiz);

        Question savedQuestion = questionRepository.save(question);

        if (dto.getAnswers() != null) {
            for (AnswerDTO aDto : dto.getAnswers()) {
                Answer answer = new Answer();
                answer.setLabel(aDto.getLabel());
                answer.setText(aDto.getText());
                answer.setIsCorrect(aDto.getIsCorrect() != null ? aDto.getIsCorrect() : false);
                answer.setQuestion(savedQuestion);
                answerRepository.save(answer);
            }
        }

        return questionRepository.findById(savedQuestion.getId()).get();
    }

    public QuizDTO toDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTimePerQuestion(quiz.getTimePerQuestion());
        dto.setShuffleQuestions(quiz.getShuffleQuestions());
        dto.setShuffleAnswers(quiz.getShuffleAnswers());
        dto.setLiveScoreboard(quiz.getLiveScoreboard());
        dto.setQuestions(quiz.getQuestions().stream()
                .map(this::toQuestionDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public QuestionDTO toQuestionDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setText(question.getText());
        dto.setPoints(question.getPoints());
        dto.setTimeLimit(question.getTimeLimit());
        dto.setMediaUrl(question.getMediaUrl());
        dto.setQuestionOrder(question.getQuestionOrder());
        dto.setAnswers(question.getAnswers().stream()
                .map(this::toAnswerDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public AnswerDTO toAnswerDTO(Answer answer) {
        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setLabel(answer.getLabel());
        dto.setText(answer.getText());
        dto.setIsCorrect(answer.getIsCorrect());
        return dto;
    }
}
