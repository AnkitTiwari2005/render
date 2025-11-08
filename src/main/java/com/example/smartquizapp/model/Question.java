package com.example.smartquizapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "option_a", columnDefinition = "TEXT", nullable = false)
    private String optionA;

    @Column(name = "option_b", columnDefinition = "TEXT", nullable = false)
    private String optionB;

    @Column(name = "option_c", columnDefinition = "TEXT")
    private String optionC;

    @Column(name = "option_d", columnDefinition = "TEXT")
    private String optionD;

    @Enumerated(EnumType.STRING)
    @Column(name = "correct_option", nullable = false, length = 1)
    private CorrectOption correctOption;

    @Column(nullable = false)
    @Builder.Default
    private Integer marks = 5;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "question_order")
    @Builder.Default
    private Integer questionOrder = 0;

    // Transient field for combined options in UI
    @Transient
    private List<String> options;

    public enum CorrectOption {
        A, B, C, D
    }

    // Helper method to get all available options
    public List<String> getOptions() {
        if (options == null) {
            options = new ArrayList<>();
            if (optionA != null && !optionA.isBlank()) options.add(optionA);
            if (optionB != null && !optionB.isBlank()) options.add(optionB);
            if (optionC != null && !optionC.isBlank()) options.add(optionC);
            if (optionD != null && !optionD.isBlank()) options.add(optionD);
        }
        return options;
    }

    @PrePersist
    protected void onCreate() {
        if (marks == null) marks = 5;
        if (questionOrder == null) questionOrder = 0;
    }
}