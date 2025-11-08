package com.example.smartquizapp.service;

import com.example.smartquizapp.model.*;
import com.example.smartquizapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepo;
    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final AttemptRepository attemptRepo;

    public QuizService(QuizRepository quizRepo, QuestionRepository questionRepo,
                      UserRepository userRepo, AttemptRepository attemptRepo) {
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.userRepo = userRepo;
        this.attemptRepo = attemptRepo;
    }

    public List<Quiz> listAllQuizzes() {
        return quizRepo.findAllByOrderByCreatedAtDesc();
    }

    public List<Quiz> listPublishedQuizzes() {
        return quizRepo.findByIsPublishedTrueOrderByCreatedAtDesc();
    }

    public Optional<Quiz> getQuizWithQuestions(Long id) {
        return quizRepo.findByIdWithQuestions(id);
    }

    @Transactional
    public Quiz createQuiz(Quiz quiz, User creator) {
        quiz.setCreatedBy(creator);
        quiz.setCreatedAt(LocalDateTime.now());
        return quizRepo.save(quiz);
    }

    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        // Overloaded method for backward compatibility
        quiz.setCreatedAt(LocalDateTime.now());
        return quizRepo.save(quiz);
    }

    @Transactional
    public Question addQuestion(Long quizId, Question question) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        question.setQuiz(quiz);

        // Set question order
        Integer maxOrder = quiz.getQuestions().stream()
                .map(Question::getQuestionOrder)
                .max(Integer::compareTo)
                .orElse(0);
        question.setQuestionOrder(maxOrder + 1);

        return questionRepo.save(question);
    }

    @Transactional
    public Question updateQuestion(Long questionId, Question questionDetails) {
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setText(questionDetails.getText());
        question.setOptionA(questionDetails.getOptionA());
        question.setOptionB(questionDetails.getOptionB());
        question.setOptionC(questionDetails.getOptionC());
        question.setOptionD(questionDetails.getOptionD());
        question.setCorrectOption(questionDetails.getCorrectOption());
        question.setMarks(questionDetails.getMarks());
        question.setExplanation(questionDetails.getExplanation());

        return questionRepo.save(question);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        questionRepo.delete(question);
    }

    @Transactional
    public Attempt evaluateAttempt(User student, Quiz quiz, Map<Long, String> answers, LocalDateTime startedAt) {
        int score = 0;
        int maxScore = 0;

        List<Question> questions = questionRepo.findByQuizId(quiz.getId());

        // Prepare answers detail map
        Map<String, Object> answersDetail = new HashMap<>();

        for (Question question : questions) {
            maxScore += question.getMarks();
            String userAnswer = answers.get(question.getId());
            String correctAnswer = question.getCorrectOption().name();
            boolean isCorrect = userAnswer != null && userAnswer.equals(correctAnswer);

            if (isCorrect) {
                score += question.getMarks();
            }

            // Store detailed answer information
            Map<String, Object> answerDetail = new HashMap<>();
            answerDetail.put("userAnswer", userAnswer);
            answerDetail.put("correctAnswer", correctAnswer);
            answerDetail.put("isCorrect", isCorrect);
            answerDetail.put("questionId", question.getId());
            answerDetail.put("questionText", question.getText());

            answersDetail.put("q_" + question.getId(), answerDetail);
        }

        LocalDateTime finishedAt = LocalDateTime.now();
        long timeTaken = Duration.between(startedAt, finishedAt).getSeconds();

        // Convert answers detail to JSON string
        String answersJson = convertMapToJson(answersDetail);

        Attempt attempt = Attempt.builder()
                .student(student)
                .quiz(quiz)
                .score(score)
                .maxScore(maxScore)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .timeTakenSeconds((int) timeTaken)
                .answersJson(answersJson)
                .build();

        return attemptRepo.save(attempt);
    }

    // Overloaded method for backward compatibility
    @Transactional
    public Attempt evaluateAttempt(User student, Quiz quiz, Map<Long, String> answers) {
        return evaluateAttempt(student, quiz, answers, LocalDateTime.now());
    }

    public List<Attempt> getLeaderboard(Long quizId) {
        return attemptRepo.findTop10ByQuizIdOrderByScoreDescFinishedAtAsc(quizId);
    }

    // Alias method for compatibility
    public List<Attempt> leaderboard(Long quizId) {
        return getLeaderboard(quizId);
    }

    public List<Attempt> getUserAttempts(Long userId, Long quizId) {
        return attemptRepo.findByStudentIdAndQuizIdOrderByFinishedAtDesc(userId, quizId);
    }

    public Map<String, Object> getQuizStatistics(Long quizId) {
        List<Attempt> attempts = attemptRepo.findByQuizId(quizId);

        if (attempts.isEmpty()) {
            return Map.of(
                    "totalAttempts", 0,
                    "averageScore", 0.0,
                    "highestScore", 0,
                    "completionRate", 0.0
            );
        }

        double averageScore = attempts.stream()
                .mapToInt(Attempt::getScore)
                .average()
                .orElse(0.0);

        int highestScore = attempts.stream()
                .mapToInt(Attempt::getScore)
                .max()
                .orElse(0);

        long completedAttempts = attempts.stream()
                .filter(a -> a.getFinishedAt() != null)
                .count();

        double completionRate = (double) completedAttempts / attempts.size() * 100;

        return Map.of(
                "totalAttempts", attempts.size(),
                "averageScore", Math.round(averageScore * 100.0) / 100.0,
                "highestScore", highestScore,
                "completionRate", Math.round(completionRate * 100.0) / 100.0
        );
    }

    @Transactional
    public void publishQuiz(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        quiz.setIsPublished(true);
        quizRepo.save(quiz);
    }

    @Transactional
    public void unpublishQuiz(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        quiz.setIsPublished(false);
        quizRepo.save(quiz);
    }

    // Helper method to convert map to JSON string (simplified)
    private String convertMapToJson(Map<String, Object> map) {
        try {
            // Simple JSON conversion - in production use Jackson ObjectMapper
            StringBuilder json = new StringBuilder("{");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                json.append("\"").append(entry.getKey()).append("\":");
                if (entry.getValue() instanceof String) {
                    json.append("\"").append(entry.getValue()).append("\"");
                } else if (entry.getValue() instanceof Boolean) {
                    json.append(entry.getValue());
                } else {
                    json.append("\"").append(entry.getValue()).append("\"");
                }
                json.append(",");
            }
            if (json.length() > 1) {
                json.setLength(json.length() - 1); // Remove trailing comma
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}