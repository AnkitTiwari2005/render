package com.example.smartquizapp.repository;

import com.example.smartquizapp.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    
    List<Attempt> findByQuizIdOrderByScoreDesc(Long quizId);
    
    List<Attempt> findByStudentIdAndQuizIdOrderByFinishedAtDesc(Long studentId, Long quizId);
    
    List<Attempt> findByQuizId(Long quizId);
    
    @Query("SELECT a FROM Attempt a WHERE a.quiz.id = :quizId ORDER BY a.score DESC, a.finishedAt ASC")
    List<Attempt> findTop10ByQuizIdOrderByScoreDescFinishedAtAsc(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.quiz.id = :quizId")
    Long countByQuizId(@Param("quizId") Long quizId);
}