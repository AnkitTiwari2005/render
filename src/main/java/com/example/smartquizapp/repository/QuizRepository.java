package com.example.smartquizapp.repository;

import com.example.smartquizapp.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    List<Quiz> findAllByOrderByCreatedAtDesc();
    
    List<Quiz> findByIsPublishedTrueOrderByCreatedAtDesc();
    
    List<Quiz> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);
    
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.createdBy.id = :userId")
    Long countByCreatedBy(@Param("userId") Long userId);
    
    boolean existsByTitleAndCreatedById(String title, Long createdById);
}