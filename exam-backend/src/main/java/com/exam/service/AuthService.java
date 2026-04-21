package com.exam.service;

import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.UserInfoDTO;

public interface AuthService {
    String login(LoginDTO loginDTO);
    void register(RegisterDTO registerDTO);
    UserInfoDTO getCurrentUserInfo();
}
