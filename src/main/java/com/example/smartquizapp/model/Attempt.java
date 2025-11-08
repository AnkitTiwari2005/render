package com.example.smartquizapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "attempt")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "max_score", nullable = false)
    @Builder.Default
    private Integer maxScore = 0;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "answers_json", columnDefinition = "json")
    private String answersJson;

    @PrePersist
    protected void onCreate() {
        if (score == null) score = 0;
        if (maxScore == null) maxScore = 0;
        if (startedAt == null) startedAt = LocalDateTime.now();
    }
}