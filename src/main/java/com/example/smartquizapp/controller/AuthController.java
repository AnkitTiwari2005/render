package com.example.smartquizapp.controller;

import com.example.smartquizapp.model.User;
import com.example.smartquizapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(HttpSession session) {
        // If user is already logged in, redirect to appropriate page
        if (session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if ("ADMIN".equals(user.getRole().name())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/quizzes";
            }
        }
        return "login";
    }

    // ✅ FIXED: Improved session management and login logic
    @PostMapping("/login")
    public String login(@RequestParam(name = "username") String username,
                       @RequestParam(name = "password") String password,
                       HttpServletRequest request,
                       HttpSession session,
                       Model model) {

        var opt = userRepo.findByUsername(username);
        if (opt.isPresent() && opt.get().getPassword().equals(password)) {
            User user = opt.get();

            // Clear any previous session and create new one
            session.invalidate();
            session = request.getSession(true);

            // Store user and role in session
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRole().name());
            session.setAttribute("username", user.getUsername());

            // Debug logging
            System.out.println("Login successful for user: " + username + " with role: " + user.getRole());

            // Redirect based on role
            if ("ADMIN".equals(user.getRole().name())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/quizzes";
            }
        }

        // If login fails
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(name = "username") String username,
                          @RequestParam(name = "password") String password,
                          HttpSession session,
                          Model model) {

        if (userRepo.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(User.UserRole.STUDENT);
        userRepo.save(u);

        // Auto-login after registration
        session.setAttribute("user", u);
        session.setAttribute("role", u.getRole().name());
        session.setAttribute("username", u.getUsername());

        model.addAttribute("success", "Registration successful. Welcome!");
        return "redirect:/quizzes";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}