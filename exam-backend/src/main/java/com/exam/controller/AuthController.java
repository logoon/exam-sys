package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.UserInfoDTO;
import com.exam.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Validated @RequestBody LoginDTO loginDTO) {
        String token = authService.login(loginDTO);
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success("登录成功", result);
    }
    
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success("注册成功", null);
    }
    
    @GetMapping("/info")
    public Result<UserInfoDTO> getCurrentUserInfo() {
        UserInfoDTO userInfo = authService.getCurrentUserInfo();
        return Result.success(userInfo);
    }
}
