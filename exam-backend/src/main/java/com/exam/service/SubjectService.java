package com.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Subject;

public interface SubjectService extends IService<Subject> {
    Page<Subject> pageSubjects(Integer current, Integer size, String name, Integer status);
}
