package com.exam.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Question;
import com.exam.entity.QuestionType;
import com.exam.entity.Subject;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionTypeMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    
    @Autowired
    private QuestionTypeMapper questionTypeMapper;
    
    @Autowired
    private SubjectMapper subjectMapper;
    
    @Override
    public List<QuestionType> getAllQuestionTypes() {
        return questionTypeMapper.selectList(null);
    }
    
    @Override
    public Page<Question> pageQuestions(Integer current, Integer size, Long subjectId, Long typeId, Integer difficulty, Integer status) {
        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        
        if (subjectId != null) {
            wrapper.eq(Question::getSubjectId, subjectId);
        }
        if (typeId != null) {
            wrapper.eq(Question::getTypeId, typeId);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (status != null) {
            wrapper.eq(Question::getStatus, status);
        }
        
        wrapper.orderByDesc(Question::getCreateTime);
        Page<Question> resultPage = this.page(page, wrapper);
        
        for (Question question : resultPage.getRecords()) {
            fillQuestionInfo(question);
        }
        
        return resultPage;
    }
    
    @Override
    public Question getQuestionDetail(Long id) {
        Question question = this.getById(id);
        if (question != null) {
            fillQuestionInfo(question);
        }
        return question;
    }
    
    private void fillQuestionInfo(Question question) {
        if (question.getSubjectId() != null) {
            Subject subject = subjectMapper.selectById(question.getSubjectId());
            if (subject != null) {
                question.setSubjectName(subject.getName());
            }
        }
        
        if (question.getTypeId() != null) {
            QuestionType type = questionTypeMapper.selectById(question.getTypeId());
            if (type != null) {
                question.setTypeName(type.getName());
                question.setTypeCode(type.getCode());
            }
        }
        
        if (StringUtils.hasText(question.getOptions())) {
            try {
                List<Question.Option> options = JSON.parseArray(question.getOptions(), Question.Option.class);
                question.setOptionList(options);
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
    }
}
