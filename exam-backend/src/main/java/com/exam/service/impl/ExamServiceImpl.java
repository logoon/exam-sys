package com.exam.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ExamPaperMapper examPaperMapper;
    
    @Autowired
    private ExamPaperQuestionMapper paperQuestionMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private ExamAnswerMapper examAnswerMapper;
    
    @Autowired
    private SubjectMapper subjectMapper;
    
    @Autowired
    private QuestionTypeMapper questionTypeMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamRecord startExam(Long paperId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null) {
            throw new RuntimeException("考卷不存在");
        }
        
        if (paper.getStatus() != 1) {
            throw new RuntimeException("考卷未启用");
        }
        
        ExamRecord existRecord = this.getOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, currentUser.getId())
                        .eq(ExamRecord::getPaperId, paperId)
                        .eq(ExamRecord::getStatus, 0)
        );
        
        if (existRecord != null) {
            return existRecord;
        }
        
        ExamRecord record = new ExamRecord();
        record.setUserId(currentUser.getId());
        record.setPaperId(paperId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(0);
        this.save(record);
        
        return record;
    }
    
    @Override
    public ExamPaper getExamQuestions(Long recordId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        ExamRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        
        if (!record.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问此考试");
        }
        
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        if (paper == null) {
            throw new RuntimeException("考卷不存在");
        }
        
        List<ExamPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, paper.getId())
                        .orderByAsc(ExamPaperQuestion::getOrderNum)
        );
        
        for (ExamPaperQuestion pq : paperQuestions) {
            Question question = questionMapper.selectById(pq.getQuestionId());
            if (question != null) {
                question.setAnswer(null);
                question.setAnalysis(null);
                if (StringUtils.hasText(question.getOptions())) {
                    try {
                        List<Question.Option> options = JSON.parseArray(question.getOptions(), Question.Option.class);
                        question.setOptionList(options);
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                }
                pq.setQuestion(question);
            }
        }
        
        paper.setQuestions(paperQuestions);
        
        Subject subject = subjectMapper.selectById(paper.getSubjectId());
        if (subject != null) {
            paper.setSubjectName(subject.getName());
        }
        
        return paper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAnswer(Long recordId, Long questionId, String userAnswer) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        ExamRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        
        if (!record.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问此考试");
        }
        
        if (record.getStatus() != 0) {
            throw new RuntimeException("考试已结束");
        }
        
        ExamAnswer existAnswer = examAnswerMapper.selectOne(
                new LambdaQueryWrapper<ExamAnswer>()
                        .eq(ExamAnswer::getRecordId, recordId)
                        .eq(ExamAnswer::getQuestionId, questionId)
        );
        
        ExamAnswer answer;
        if (existAnswer != null) {
            answer = existAnswer;
            answer.setUserAnswer(userAnswer);
            examAnswerMapper.updateById(answer);
        } else {
            answer = new ExamAnswer();
            answer.setRecordId(recordId);
            answer.setQuestionId(questionId);
            answer.setUserAnswer(userAnswer);
            examAnswerMapper.insert(answer);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamRecord finishExam(Long recordId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        ExamRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        
        if (!record.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问此考试");
        }
        
        if (record.getStatus() != 0) {
            return record;
        }
        
        record.setEndTime(LocalDateTime.now());
        record.setStatus(1);
        
        if (record.getStartTime() != null) {
            Duration duration = Duration.between(record.getStartTime(), record.getEndTime());
            record.setDuration((int) duration.getSeconds());
        }
        
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        
        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId)
        );
        
        int totalScore = 0;
        
        for (ExamAnswer answer : answers) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question == null) {
                continue;
            }
            
            ExamPaperQuestion pq = paperQuestionMapper.selectOne(
                    new LambdaQueryWrapper<ExamPaperQuestion>()
                            .eq(ExamPaperQuestion::getPaperId, record.getPaperId())
                            .eq(ExamPaperQuestion::getQuestionId, question.getId())
            );
            
            int score = pq != null ? pq.getScore() : 0;
            
            QuestionType type = questionTypeMapper.selectById(question.getTypeId());
            boolean isCorrect = false;
            
            if (type != null) {
                switch (type.getCode()) {
                    case "SINGLE":
                    case "MULTIPLE":
                        if (answer.getUserAnswer() != null && answer.getUserAnswer().equals(question.getAnswer())) {
                            isCorrect = true;
                        }
                        break;
                    case "FILL":
                        if (answer.getUserAnswer() != null && answer.getUserAnswer().trim().equals(question.getAnswer().trim())) {
                            isCorrect = true;
                        }
                        break;
                    case "ESSAY":
                        isCorrect = false;
                        score = 0;
                        break;
                }
            }
            
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answer.setScore(isCorrect ? score : 0);
            examAnswerMapper.updateById(answer);
            
            totalScore += answer.getScore();
        }
        
        record.setTotalScore(totalScore);
        
        if (paper != null && paper.getPassScore() != null) {
            record.setIsPassed(totalScore >= paper.getPassScore() ? 1 : 0);
        } else {
            record.setIsPassed(0);
        }
        
        this.updateById(record);
        
        return record;
    }
    
    @Override
    public ExamRecord getExamDetail(Long recordId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        ExamRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        
        if (!record.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问此考试");
        }
        
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        if (paper != null) {
            record.setPaperName(paper.getName());
        }
        
        return record;
    }
    
    @Override
    public Page<ExamRecord> getExamRecords(Integer current, Integer size, Integer status) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Page<ExamRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(ExamRecord::getUserId, currentUser.getId());
        
        if (status != null) {
            wrapper.eq(ExamRecord::getStatus, status);
        }
        
        wrapper.orderByDesc(ExamRecord::getCreateTime);
        Page<ExamRecord> resultPage = this.page(page, wrapper);
        
        for (ExamRecord record : resultPage.getRecords()) {
            ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
            if (paper != null) {
                record.setPaperName(paper.getName());
            }
        }
        
        return resultPage;
    }
    
    @Override
    public List<ExamAnswer> getExamAnswers(Long recordId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        ExamRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        
        if (!record.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问此考试");
        }
        
        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId)
        );
        
        for (ExamAnswer answer : answers) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question != null) {
                if (StringUtils.hasText(question.getOptions())) {
                    try {
                        List<Question.Option> options = JSON.parseArray(question.getOptions(), Question.Option.class);
                        question.setOptionList(options);
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                }
                answer.setQuestion(question);
            }
        }
        
        return answers;
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, userDetails.getUsername())
            );
        }
        return null;
    }
}
