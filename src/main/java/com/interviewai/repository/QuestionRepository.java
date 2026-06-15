package com.interviewai.repository;

import com.interviewai.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCategory(String category);

    @Query("SELECT DISTINCT q.category FROM Question q")
    List<String> findDistinctCategories();

    @Modifying
    @Transactional
    @Query(value = """
            DELETE q1 FROM questions q1
            INNER JOIN questions q2 ON q1.title = q2.title AND q1.id < q2.id
            """, nativeQuery = true)
    int deleteDuplicateQuestions();
}
