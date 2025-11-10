package com.phong.hrm_backend.controller;

import com.phong.hrm_backend.dto.request.LoginRequest;
import com.phong.hrm_backend.dto.request.RegisterRequest;
import com.phong.hrm_backend.dto.response.AuthResponse;
import com.phong.hrm_backend.dto.response.UserResponseDTO;
import com.phong.hrm_backend.service.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequest registerRequest){
        return new ResponseEntity<>(authService.register(registerRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody LoginRequest loginRequest){
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authResponse.getJwtCookie().toString())
                .body(authResponse.getUserInfo());
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        ResponseCookie clearJwtCookie = authService.logout(httpServletRequest, httpServletResponse);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearJwtCookie.toString())
                .body("Logout successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserResponseDTO> refresh(@RequestBody Map<String, String> refreshRequest){
        AuthResponse authResponse = authService.refreshAccessToken(refreshRequest.get("refreshToken"));
        return ResponseEntity.ok()
                .body(authResponse.getUserInfo());
    }
}
