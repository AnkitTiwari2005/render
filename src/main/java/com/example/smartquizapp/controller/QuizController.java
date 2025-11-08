package com.example.smartquizapp.controller;

import com.example.smartquizapp.model.*;
import com.example.smartquizapp.repository.*;
import com.example.smartquizapp.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/quizzes")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private AttemptRepository attemptRepository;

    // ✅ List all quizzes
    @GetMapping
    public String listQuizzes(Model model) {
        model.addAttribute("quizzes", quizRepository.findAll());
        return "quiz/list";
    }

    // ✅ Start a quiz - FIXED NULL POINTER ISSUE
    @GetMapping("/{id}/start")
    public String startQuiz(@PathVariable("id") Long id, Model model, HttpSession session) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        List<Question> questions = questionRepository.findByQuizId(id);

        for (Question q : questions) {
            // Use the actual option fields from database
            List<String> options = new ArrayList<>();
            if (q.getOptionA() != null && !q.getOptionA().isEmpty()) options.add(q.getOptionA());
            if (q.getOptionB() != null && !q.getOptionB().isEmpty()) options.add(q.getOptionB());
            if (q.getOptionC() != null && !q.getOptionC().isEmpty()) options.add(q.getOptionC());
            if (q.getOptionD() != null && !q.getOptionD().isEmpty()) options.add(q.getOptionD());

            q.setOptions(options);
        }

        // Store start time in session
        session.setAttribute("quizStartTime_" + id, LocalDateTime.now());
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        return "quiz/start";
    }

    // ✅ NEW: Submit quiz and calculate score
    @PostMapping("/{id}/submit")
    public String submitQuiz(@PathVariable("id") Long id,
                           @RequestParam Map<String, String> allParams,
                           HttpSession session,
                           Model model) {

        Quiz quiz = quizRepository.findById(id).orElseThrow();
        User student = (User) session.getAttribute("user");

        if (student == null) {
            return "redirect:/login";
        }

        // Extract answers (parameters like "q_1", "q_2", etc.)
        Map<Long, String> answers = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("q_")) {
                try {
                    Long questionId = Long.parseLong(entry.getKey().substring(2));
                    answers.put(questionId, entry.getValue());
                } catch (NumberFormatException e) {
                    // Skip invalid question IDs
                }
            }
        }

        // Get start time from session
        LocalDateTime startedAt = (LocalDateTime) session.getAttribute("quizStartTime_" + id);
        if (startedAt == null) {
            startedAt = LocalDateTime.now(); // fallback
        }

        // Evaluate the attempt
        Attempt attempt = quizService.evaluateAttempt(student, quiz, answers, startedAt);

        model.addAttribute("score", attempt.getScore());
        model.addAttribute("totalQuestions", quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);
        return "quiz/result";
    }

    // ✅ NEW: View leaderboard for a quiz
    @GetMapping("/{id}/leaderboard")
    public String leaderboard(@PathVariable("id") Long id, Model model) {
        List<Attempt> leaders = quizService.getLeaderboard(id);
        model.addAttribute("leaders", leaders);
        model.addAttribute("quiz", quizRepository.findById(id).orElseThrow());
        return "quiz/leaderboard";
    }
}