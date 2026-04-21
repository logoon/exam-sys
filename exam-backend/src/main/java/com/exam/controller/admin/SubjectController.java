package com.exam.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.dto.PageDTO;
import com.exam.entity.Subject;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subjects")
public class SubjectController {
    
    @Autowired
    private SubjectService subjectService;
    
    @GetMapping("/list")
    public Result<List<Subject>> listAll() {
        List<Subject> list = subjectService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    public Result<PageDTO<Subject>> pageSubjects(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        Page<Subject> page = subjectService.pageSubjects(current, size, name, status);
        PageDTO<Subject> pageDTO = new PageDTO<>();
        pageDTO.setRecords(page.getRecords());
        pageDTO.setTotal(page.getTotal());
        pageDTO.setCurrent(page.getCurrent());
        pageDTO.setSize(page.getSize());
        pageDTO.setPages(page.getPages());
        return Result.success(pageDTO);
    }
    
    @GetMapping("/{id}")
    public Result<Subject> getById(@PathVariable Long id) {
        Subject subject = subjectService.getById(id);
        if (subject == null) {
            return Result.error("科目不存在");
        }
        return Result.success(subject);
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody Subject subject) {
        subject.setStatus(1);
        subjectService.save(subject);
        return Result.success();
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Subject subject) {
        subject.setId(id);
        subjectService.updateById(subject);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        subjectService.removeById(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Subject subject = subjectService.getById(id);
        if (subject == null) {
            return Result.error("科目不存在");
        }
        subject.setStatus(status);
        subjectService.updateById(subject);
        return Result.success();
    }
}
