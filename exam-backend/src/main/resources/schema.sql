-- 模拟考试系统数据库初始化脚本
-- 数据库: exam_system

-- 创建数据库
CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE exam_system;

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `fk_role` (`role_id`),
  CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 科目表
CREATE TABLE IF NOT EXISTS `subject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '科目ID',
  `name` varchar(100) NOT NULL COMMENT '科目名称',
  `code` varchar(50) DEFAULT NULL COMMENT '科目编码',
  `description` varchar(500) DEFAULT NULL COMMENT '科目描述',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目表';

-- 题目类型表
CREATE TABLE IF NOT EXISTS `question_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `name` varchar(50) NOT NULL COMMENT '类型名称',
  `code` varchar(50) NOT NULL COMMENT '类型编码：SINGLE-单选，MULTIPLE-多选，FILL-填空，ESSAY-简答',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目类型表';

-- 题目表
CREATE TABLE IF NOT EXISTS `question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `subject_id` bigint(20) NOT NULL COMMENT '科目ID',
  `type_id` bigint(20) NOT NULL COMMENT '题目类型ID',
  `content` text NOT NULL COMMENT '题目内容',
  `options` text COMMENT '选项（JSON格式，单选/多选题使用）',
  `answer` text NOT NULL COMMENT '参考答案',
  `analysis` text COMMENT '答案解析',
  `score` int(11) DEFAULT 1 COMMENT '题目分值',
  `difficulty` tinyint(4) DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_question_subject` (`subject_id`),
  KEY `fk_question_type` (`type_id`),
  CONSTRAINT `fk_question_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `fk_question_type` FOREIGN KEY (`type_id`) REFERENCES `question_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 考卷表
CREATE TABLE IF NOT EXISTS `exam_paper` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '考卷ID',
  `name` varchar(200) NOT NULL COMMENT '考卷名称',
  `subject_id` bigint(20) NOT NULL COMMENT '科目ID',
  `total_score` int(11) NOT NULL COMMENT '总分',
  `pass_score` int(11) NOT NULL COMMENT '及格分数',
  `duration` int(11) NOT NULL COMMENT '考试时长（分钟）',
  `description` varchar(500) DEFAULT NULL COMMENT '考卷描述',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_paper_subject` (`subject_id`),
  CONSTRAINT `fk_paper_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考卷表';

-- 考卷题目关联表
CREATE TABLE IF NOT EXISTS `exam_paper_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `paper_id` bigint(20) NOT NULL COMMENT '考卷ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `order_num` int(11) DEFAULT 0 COMMENT '题目序号',
  `score` int(11) NOT NULL COMMENT '题目分值',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_question` (`paper_id`, `question_id`),
  KEY `fk_question` (`question_id`),
  CONSTRAINT `fk_paper` FOREIGN KEY (`paper_id`) REFERENCES `exam_paper` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考卷题目关联表';

-- 考试记录表
CREATE TABLE IF NOT EXISTS `exam_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '考试记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `paper_id` bigint(20) NOT NULL COMMENT '考卷ID',
  `total_score` int(11) DEFAULT 0 COMMENT '得分',
  `is_passed` tinyint(1) DEFAULT 0 COMMENT '是否及格：1-是，0-否',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int(11) DEFAULT 0 COMMENT '实际用时（秒）',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态：0-进行中，1-已完成，2-已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_record_user` (`user_id`),
  KEY `fk_record_paper` (`paper_id`),
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_record_paper` FOREIGN KEY (`paper_id`) REFERENCES `exam_paper` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 考试答案记录表
CREATE TABLE IF NOT EXISTS `exam_answer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '答案记录ID',
  `record_id` bigint(20) NOT NULL COMMENT '考试记录ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `user_answer` text COMMENT '用户答案',
  `is_correct` tinyint(1) DEFAULT 0 COMMENT '是否正确：1-是，0-否',
  `score` int(11) DEFAULT 0 COMMENT '得分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_question` (`record_id`, `question_id`),
  KEY `fk_answer_question` (`question_id`),
  CONSTRAINT `fk_answer_record` FOREIGN KEY (`record_id`) REFERENCES `exam_record` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试答案记录表';

-- 初始化数据
-- 插入角色
INSERT INTO `role` (`name`, `code`, `description`) VALUES 
('管理员', 'ADMIN', '系统管理员，拥有所有权限'),
('普通用户', 'USER', '普通用户，只能参加考试');

-- 插入题目类型
INSERT INTO `question_type` (`name`, `code`, `description`) VALUES 
('单选题', 'SINGLE', '单项选择题'),
('多选题', 'MULTIPLE', '多项选择题'),
('填空题', 'FILL', '填空题'),
('简答题', 'ESSAY', '简答题');

-- 插入默认管理员账号（密码：admin123，使用BCrypt加密）
INSERT INTO `user` (`username`, `password`, `real_name`, `status`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '系统管理员', 1);

-- 给管理员分配角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);
