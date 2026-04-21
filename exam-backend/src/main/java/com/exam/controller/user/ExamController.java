package com.exam.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.dto.PageDTO;
import com.exam.entity.ExamAnswer;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamRecord;
import com.exam.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/exam")
public class ExamController {
    
    @Autowired
    private ExamService examService;
    
    @PostMapping("/start/{paperId}")
    public Result<ExamRecord> startExam(@PathVariable Long paperId) {
        ExamRecord record = examService.startExam(paperId);
        return Result.success(record);
    }
    
    @GetMapping("/{recordId}/questions")
    public Result<ExamPaper> getExamQuestions(@PathVariable Long recordId) {
        ExamPaper paper = examService.getExamQuestions(recordId);
        return Result.success(paper);
    }
    
    @PostMapping("/{recordId}/answer")
    public Result<Void> submitAnswer(
            @PathVariable Long recordId,
            @RequestBody Map<String, Object> params
    ) {
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String userAnswer = params.get("userAnswer") != null ? params.get("userAnswer").toString() : null;
        examService.submitAnswer(recordId, questionId, userAnswer);
        return Result.success();
    }
    
    @PostMapping("/{recordId}/finish")
    public Result<ExamRecord> finishExam(@PathVariable Long recordId) {
        ExamRecord record = examService.finishExam(recordId);
        return Result.success(record);
    }
    
    @GetMapping("/records")
    public Result<PageDTO<ExamRecord>> getExamRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        Page<ExamRecord> page = examService.getExamRecords(current, size, status);
        PageDTO<ExamRecord> pageDTO = new PageDTO<>();
        pageDTO.setRecords(page.getRecords());
        pageDTO.setTotal(page.getTotal());
        pageDTO.setCurrent(page.getCurrent());
        pageDTO.setSize(page.getSize());
        pageDTO.setPages(page.getPages());
        return Result.success(pageDTO);
    }
    
    @GetMapping("/records/{recordId}")
    public Result<ExamRecord> getExamDetail(@PathVariable Long recordId) {
        ExamRecord record = examService.getExamDetail(recordId);
        return Result.success(record);
    }
    
    @GetMapping("/records/{recordId}/answers")
    public Result<List<ExamAnswer>> getExamAnswers(@PathVariable Long recordId) {
        List<ExamAnswer> answers = examService.getExamAnswers(recordId);
        return Result.success(answers);
    }
}
