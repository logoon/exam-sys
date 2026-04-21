package com.exam.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private List<String> roles;
}
