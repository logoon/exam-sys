package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamPaperQuestion;
import com.exam.entity.Question;
import com.exam.entity.Subject;
import com.exam.mapper.ExamPaperMapper;
import com.exam.mapper.ExamPaperQuestionMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamPaperServiceImpl extends ServiceImpl<ExamPaperMapper, ExamPaper> implements ExamPaperService {
    
    @Autowired
    private ExamPaperQuestionMapper paperQuestionMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private SubjectMapper subjectMapper;
    
    @Override
    public Page<ExamPaper> pagePapers(Integer current, Integer size, Long subjectId, Integer status, Boolean isAdmin) {
        Page<ExamPaper> page = new Page<>(current, size);
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        
        if (subjectId != null) {
            wrapper.eq(ExamPaper::getSubjectId, subjectId);
        }
        if (status != null) {
            wrapper.eq(ExamPaper::getStatus, status);
        }
        if (!isAdmin) {
            wrapper.eq(ExamPaper::getStatus, 1);
        }
        
        wrapper.orderByDesc(ExamPaper::getCreateTime);
        Page<ExamPaper> resultPage = this.page(page, wrapper);
        
        for (ExamPaper paper : resultPage.getRecords()) {
            fillPaperInfo(paper);
        }
        
        return resultPage;
    }
    
    @Override
    public ExamPaper getPaperDetail(Long id, Boolean includeQuestions) {
        ExamPaper paper = this.getById(id);
        if (paper == null) {
            return null;
        }
        
        fillPaperInfo(paper);
        
        if (includeQuestions) {
            List<ExamPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                    new LambdaQueryWrapper<ExamPaperQuestion>()
                            .eq(ExamPaperQuestion::getPaperId, id)
                            .orderByAsc(ExamPaperQuestion::getOrderNum)
            );
            
            for (ExamPaperQuestion pq : paperQuestions) {
                Question question = questionMapper.selectById(pq.getQuestionId());
                pq.setQuestion(question);
            }
            
            paper.setQuestions(paperQuestions);
        }
        
        return paper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPaperWithQuestions(ExamPaper paper, List<ExamPaperQuestion> questions) {
        paper.setStatus(1);
        this.save(paper);
        
        if (questions != null && !questions.isEmpty()) {
            int orderNum = 1;
            for (ExamPaperQuestion pq : questions) {
                pq.setPaperId(paper.getId());
                pq.setOrderNum(orderNum++);
                paperQuestionMapper.insert(pq);
            }
            
            int totalScore = questions.stream()
                    .mapToInt(ExamPaperQuestion::getScore)
                    .sum();
            paper.setTotalScore(totalScore);
            this.updateById(paper);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaperWithQuestions(Long id, ExamPaper paper, List<ExamPaperQuestion> questions) {
        paper.setId(id);
        this.updateById(paper);
        
        if (questions != null) {
            paperQuestionMapper.delete(
                    new LambdaQueryWrapper<ExamPaperQuestion>().eq(ExamPaperQuestion::getPaperId, id)
            );
            
            if (!questions.isEmpty()) {
                int orderNum = 1;
                for (ExamPaperQuestion pq : questions) {
                    pq.setPaperId(id);
                    pq.setOrderNum(orderNum++);
                    paperQuestionMapper.insert(pq);
                }
                
                int totalScore = questions.stream()
                        .mapToInt(ExamPaperQuestion::getScore)
                        .sum();
                ExamPaper updatePaper = new ExamPaper();
                updatePaper.setId(id);
                updatePaper.setTotalScore(totalScore);
                this.updateById(updatePaper);
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQuestionsToPaper(Long paperId, List<ExamPaperQuestion> questions) {
        ExamPaper paper = this.getById(paperId);
        if (paper == null) {
            throw new RuntimeException("考卷不存在");
        }
        
        if (questions != null && !questions.isEmpty()) {
            Integer maxOrder = paperQuestionMapper.selectOne(
                    new LambdaQueryWrapper<ExamPaperQuestion>()
                            .eq(ExamPaperQuestion::getPaperId, paperId)
                            .orderByDesc(ExamPaperQuestion::getOrderNum)
                            .last("LIMIT 1")
            ) != null ? paperQuestionMapper.selectOne(
                    new LambdaQueryWrapper<ExamPaperQuestion>()
                            .eq(ExamPaperQuestion::getPaperId, paperId)
                            .orderByDesc(ExamPaperQuestion::getOrderNum)
                            .last("LIMIT 1")
            ).getOrderNum() : 0;
            
            int orderNum = (maxOrder != null ? maxOrder : 0) + 1;
            for (ExamPaperQuestion pq : questions) {
                pq.setPaperId(paperId);
                pq.setOrderNum(orderNum++);
                paperQuestionMapper.insert(pq);
            }
            
            int addedScore = questions.stream()
                    .mapToInt(ExamPaperQuestion::getScore)
                    .sum();
            paper.setTotalScore((paper.getTotalScore() != null ? paper.getTotalScore() : 0) + addedScore);
            this.updateById(paper);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeQuestionFromPaper(Long paperId, Long questionId) {
        ExamPaper paper = this.getById(paperId);
        if (paper == null) {
            throw new RuntimeException("考卷不存在");
        }
        
        ExamPaperQuestion pq = paperQuestionMapper.selectOne(
                new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, paperId)
                        .eq(ExamPaperQuestion::getQuestionId, questionId)
        );
        
        if (pq != null) {
            int removedScore = pq.getScore();
            paperQuestionMapper.deleteById(pq.getId());
            
            paper.setTotalScore((paper.getTotalScore() != null ? paper.getTotalScore() : 0) - removedScore);
            this.updateById(paper);
        }
    }
    
    @Override
    public void updatePaperStatus(Long id, Integer status) {
        ExamPaper paper = this.getById(id);
        if (paper == null) {
            throw new RuntimeException("考卷不存在");
        }
        paper.setStatus(status);
        this.updateById(paper);
    }
    
    private void fillPaperInfo(ExamPaper paper) {
        if (paper.getSubjectId() != null) {
            Subject subject = subjectMapper.selectById(paper.getSubjectId());
            if (subject != null) {
                paper.setSubjectName(subject.getName());
            }
        }
    }
}
