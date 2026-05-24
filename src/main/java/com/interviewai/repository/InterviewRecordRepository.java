package com.interviewai.repository;

import com.interviewai.entity.InterviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRecordRepository extends JpaRepository<InterviewRecord, Long> {
    List<InterviewRecord> findByUserIdOrderByCreateTimeDesc(Long userId);
    long countByUserId(Long userId);
    
    @org.springframework.transaction.annotation.Transactional
    void deleteByUserId(Long userId);
}
