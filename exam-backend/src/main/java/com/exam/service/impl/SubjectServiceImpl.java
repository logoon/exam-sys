package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Subject;
import com.exam.mapper.SubjectMapper;
import com.exam.service.SubjectService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {
    
    @Override
    public Page<Subject> pageSubjects(Integer current, Integer size, String name, Integer status) {
        Page<Subject> page = new Page<>(current, size);
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(Subject::getName, name);
        }
        if (status != null) {
            wrapper.eq(Subject::getStatus, status);
        }
        
        wrapper.orderByDesc(Subject::getCreateTime);
        return this.page(page, wrapper);
    }
}
