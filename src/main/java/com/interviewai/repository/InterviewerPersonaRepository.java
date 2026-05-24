package com.interviewai.repository;

import com.interviewai.entity.InterviewerPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewerPersonaRepository extends JpaRepository<InterviewerPersona, Long> {
}
