package com.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamPaperQuestion;

import java.util.List;

public interface ExamPaperService extends IService<ExamPaper> {
    Page<ExamPaper> pagePapers(Integer current, Integer size, Long subjectId, Integer status, Boolean isAdmin);
    ExamPaper getPaperDetail(Long id, Boolean includeQuestions);
    void createPaperWithQuestions(ExamPaper paper, List<ExamPaperQuestion> questions);
    void updatePaperWithQuestions(Long id, ExamPaper paper, List<ExamPaperQuestion> questions);
    void addQuestionsToPaper(Long paperId, List<ExamPaperQuestion> questions);
    void removeQuestionFromPaper(Long paperId, Long questionId);
    void updatePaperStatus(Long id, Integer status);
}
