package com.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.ExamAnswer;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamRecord;

import java.util.List;

public interface ExamService extends IService<ExamRecord> {
    ExamRecord startExam(Long paperId);
    ExamPaper getExamQuestions(Long recordId);
    void submitAnswer(Long recordId, Long questionId, String userAnswer);
    ExamRecord finishExam(Long recordId);
    ExamRecord getExamDetail(Long recordId);
    Page<ExamRecord> getExamRecords(Integer current, Integer size, Integer status);
    List<ExamAnswer> getExamAnswers(Long recordId);
}
