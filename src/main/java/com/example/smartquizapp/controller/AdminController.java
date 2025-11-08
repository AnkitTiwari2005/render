package com.example.smartquizapp.controller;

import com.example.smartquizapp.model.*;
import com.example.smartquizapp.repository.*;
import com.example.smartquizapp.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final QuizService quizService;
    private final UserRepository userRepo;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public AdminController(QuizService quizService,
                          UserRepository userRepo,
                          QuizRepository quizRepository,
                          QuestionRepository questionRepository) {
        this.quizService = quizService;
        this.userRepo = userRepo;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    // ✅ FIXED: Better session checking with debug info
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Check if user is admin
        User user = (User) session.getAttribute("user");
        System.out.println("Dashboard access attempt - User: " + (user != null ? user.getUsername() : "null") + 
                          ", Role: " + (user != null ? user.getRole() : "null"));
        
        if (user == null) {
            System.out.println("No user in session - redirecting to login");
            return "redirect:/login";
        }
        
        if (!"ADMIN".equals(user.getRole().name())) {
            System.out.println("User is not ADMIN - redirecting to quizzes");
            return "redirect:/quizzes";
        }
        
        model.addAttribute("quizzes", quizService.listAllQuizzes());
        System.out.println("Admin dashboard accessed successfully by: " + user.getUsername());
        return "admin/dashboard";
    }

    @GetMapping("/quiz/new")
    public String newQuizPage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole().name())) {
            return "redirect:/login";
        }
        return "admin/new-quiz";
    }

    @PostMapping("/quiz")
    public String createQuiz(@RequestParam(name = "title") String title,
                            @RequestParam(name = "durationSeconds") Integer durationSeconds,
                            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole().name())) {
            return "redirect:/login";
        }
        Quiz q = Quiz.builder()
                .title(title)
                .durationSeconds(durationSeconds)
                .build();
        quizService.createQuiz(q, user);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/quiz/{id}/question/new")
    public String addQuestionPage(@PathVariable(name = "id") Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole().name())) {
            return "redirect:/login";
        }
        model.addAttribute("quizId", id);
        return "admin/add-question";
    }

    @PostMapping("/quiz/{id}/question")
    public String addQuestion(@PathVariable(name = "id") Long id,
                             @RequestParam(name = "text") String text,
                             @RequestParam(name = "optionA") String optionA,
                             @RequestParam(name = "optionB") String optionB,
                             @RequestParam(name = "optionC") String optionC,
                             @RequestParam(name = "optionD") String optionD,
                             @RequestParam(name = "correctOption") String correctOption,
                             @RequestParam(name = "marks", defaultValue = "5") Integer marks,
                             HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole().name())) {
            return "redirect:/login";
        }

        Question q = Question.builder()
                .text(text)
                .optionA(optionA)
                .optionB(optionB)
                .optionC(optionC)
                .optionD(optionD)
                .correctOption(Question.CorrectOption.valueOf(correctOption))
                .marks(marks)
                .build();

        quizService.addQuestion(id, q);
        return "redirect:/admin/dashboard";
    }

    // ✅ DELETE QUIZ FUNCTIONALITY
    @PostMapping("/quiz/{id}/delete")
    public String deleteQuiz(@PathVariable("id") Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole().name())) {
            return "redirect:/login";
        }

        // Delete all questions belonging to the quiz first
        List<Question> questions = questionRepository.findByQuizId(id);
        if (questions != null && !questions.isEmpty()) {
            questionRepository.deleteAll(questions);
        }

        // Then delete the quiz
        quizRepository.deleteById(id);

        return "redirect:/admin/dashboard";
    }
}