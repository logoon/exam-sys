package com.exam.controller.admin;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.dto.PageDTO;
import com.exam.entity.Question;
import com.exam.entity.QuestionType;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/questions")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @GetMapping("/types")
    public Result<List<QuestionType>> getQuestionTypes() {
        List<QuestionType> types = questionService.getAllQuestionTypes();
        return Result.success(types);
    }
    
    @GetMapping("/page")
    public Result<PageDTO<Question>> pageQuestions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer status
    ) {
        Page<Question> page = questionService.pageQuestions(current, size, subjectId, typeId, difficulty, status);
        PageDTO<Question> pageDTO = new PageDTO<>();
        pageDTO.setRecords(page.getRecords());
        pageDTO.setTotal(page.getTotal());
        pageDTO.setCurrent(page.getCurrent());
        pageDTO.setSize(page.getSize());
        pageDTO.setPages(page.getPages());
        return Result.success(pageDTO);
    }
    
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        Question question = questionService.getQuestionDetail(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        return Result.success(question);
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody Question question) {
        if (question.getOptionList() != null && !question.getOptionList().isEmpty()) {
            question.setOptions(JSON.toJSONString(question.getOptionList()));
        }
        if (question.getScore() == null) {
            question.setScore(1);
        }
        if (question.getDifficulty() == null) {
            question.setDifficulty(1);
        }
        question.setStatus(1);
        questionService.save(question);
        return Result.success();
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        if (question.getOptionList() != null && !question.getOptionList().isEmpty()) {
            question.setOptions(JSON.toJSONString(question.getOptionList()));
        }
        questionService.updateById(question);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.removeById(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        question.setStatus(status);
        questionService.updateById(question);
        return Result.success();
    }
}
