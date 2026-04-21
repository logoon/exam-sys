package com.exam.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.dto.PageDTO;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamPaperQuestion;
import com.exam.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/papers")
public class ExamPaperAdminController {
    
    @Autowired
    private ExamPaperService examPaperService;
    
    @GetMapping("/page")
    public Result<PageDTO<ExamPaper>> pagePapers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer status
    ) {
        Page<ExamPaper> page = examPaperService.pagePapers(current, size, subjectId, status, true);
        PageDTO<ExamPaper> pageDTO = new PageDTO<>();
        pageDTO.setRecords(page.getRecords());
        pageDTO.setTotal(page.getTotal());
        pageDTO.setCurrent(page.getCurrent());
        pageDTO.setSize(page.getSize());
        pageDTO.setPages(page.getPages());
        return Result.success(pageDTO);
    }
    
    @GetMapping("/{id}")
    public Result<ExamPaper> getById(@PathVariable Long id,
                                     @RequestParam(defaultValue = "true") Boolean includeQuestions) {
        ExamPaper paper = examPaperService.getPaperDetail(id, includeQuestions);
        if (paper == null) {
            return Result.error("考卷不存在");
        }
        return Result.success(paper);
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody ExamPaper paper) {
        examPaperService.createPaperWithQuestions(paper, paper.getQuestions());
        return Result.success();
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ExamPaper paper) {
        examPaperService.updatePaperWithQuestions(id, paper, paper.getQuestions());
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examPaperService.removeById(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        examPaperService.updatePaperStatus(id, status);
        return Result.success();
    }
    
    @PostMapping("/{paperId}/questions")
    public Result<Void> addQuestions(@PathVariable Long paperId, @RequestBody List<ExamPaperQuestion> questions) {
        examPaperService.addQuestionsToPaper(paperId, questions);
        return Result.success();
    }
    
    @DeleteMapping("/{paperId}/questions/{questionId}")
    public Result<Void> removeQuestion(@PathVariable Long paperId, @PathVariable Long questionId) {
        examPaperService.removeQuestionFromPaper(paperId, questionId);
        return Result.success();
    }
}
