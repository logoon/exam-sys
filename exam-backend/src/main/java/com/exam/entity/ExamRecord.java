package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("exam_record")
public class ExamRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long paperId;
    
    private Integer totalScore;
    
    private Integer isPassed;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer duration;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String paperName;
    
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private List<ExamAnswer> answers;
}
