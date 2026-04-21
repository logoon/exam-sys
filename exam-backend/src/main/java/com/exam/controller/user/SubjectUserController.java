package com.exam.controller.user;

import com.exam.common.Result;
import com.exam.entity.Subject;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/subjects")
public class SubjectUserController {
    
    @Autowired
    private SubjectService subjectService;
    
    @GetMapping("/list")
    public Result<List<Subject>> listAll() {
        List<Subject> list = subjectService.list();
        return Result.success(list);
    }
}
