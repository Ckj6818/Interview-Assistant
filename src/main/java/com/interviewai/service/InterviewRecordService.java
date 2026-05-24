package com.interviewai.service;

import com.interviewai.entity.InterviewRecord;
import com.interviewai.repository.InterviewRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewRecordService {

    @Autowired
    private InterviewRecordRepository interviewRecordRepository;

    public long getInterviewCountByUserId(Long userId) {
        return interviewRecordRepository.countByUserId(userId);
    }

    public List<InterviewRecord> getRecordsByUserId(Long userId) {
        return interviewRecordRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
}
