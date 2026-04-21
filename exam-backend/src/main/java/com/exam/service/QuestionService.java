package com.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Question;
import com.exam.entity.QuestionType;

import java.util.List;

public interface QuestionService extends IService<Question> {
    List<QuestionType> getAllQuestionTypes();
    Page<Question> pageQuestions(Integer current, Integer size, Long subjectId, Long typeId, Integer difficulty, Integer status);
    Question getQuestionDetail(Long id);
}
