package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long paperId;
    
    private Long questionId;
    
    private Integer orderNum;
    
    private Integer score;
    
    @TableField(exist = false)
    private Question question;
}
