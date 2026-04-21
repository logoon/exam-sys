package com.exam.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.dto.PageDTO;
import com.exam.entity.ExamPaper;
import com.exam.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/papers")
public class ExamPaperUserController {
    
    @Autowired
    private ExamPaperService examPaperService;
    
    @GetMapping("/list")
    public Result<List<ExamPaper>> listAll(
            @RequestParam(required = false) Long subjectId
    ) {
        Page<ExamPaper> page = examPaperService.pagePapers(1, 1000, subjectId, 1, false);
        return Result.success(page.getRecords());
    }
    
    @GetMapping("/page")
    public Result<PageDTO<ExamPaper>> pagePapers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId
    ) {
        Page<ExamPaper> page = examPaperService.pagePapers(current, size, subjectId, 1, false);
        PageDTO<ExamPaper> pageDTO = new PageDTO<>();
        pageDTO.setRecords(page.getRecords());
        pageDTO.setTotal(page.getTotal());
        pageDTO.setCurrent(page.getCurrent());
        pageDTO.setSize(page.getSize());
        pageDTO.setPages(page.getPages());
        return Result.success(pageDTO);
    }
    
    @GetMapping("/{id}")
    public Result<ExamPaper> getById(@PathVariable Long id) {
        ExamPaper paper = examPaperService.getPaperDetail(id, false);
        if (paper == null) {
            return Result.error("考卷不存在");
        }
        return Result.success(paper);
    }
    
    @GetMapping("/{id}/questions")
    public Result<ExamPaper> getPaperWithQuestions(@PathVariable Long id) {
        ExamPaper paper = examPaperService.getPaperDetail(id, true);
        if (paper == null) {
            return Result.error("考卷不存在");
        }
        return Result.success(paper);
    }
}
