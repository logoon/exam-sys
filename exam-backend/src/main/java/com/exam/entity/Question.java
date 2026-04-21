package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("question")
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long subjectId;
    
    private Long typeId;
    
    private String content;
    
    private String options;
    
    private String answer;
    
    private String analysis;
    
    private Integer score;
    
    private Integer difficulty;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String subjectName;
    
    @TableField(exist = false)
    private String typeName;
    
    @TableField(exist = false)
    private String typeCode;
    
    @TableField(exist = false)
    private List<Option> optionList;
    
    @Data
    public static class Option {
        private String label;
        private String content;
    }
}
