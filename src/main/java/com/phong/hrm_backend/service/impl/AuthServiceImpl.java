package com.phong.hrm_backend.service.impl;

import com.phong.hrm_backend.dto.request.LoginRequest;
import com.phong.hrm_backend.dto.request.RegisterRequest;
import com.phong.hrm_backend.dto.response.AuthResponse;
import com.phong.hrm_backend.dto.response.UserResponseDTO;
import com.phong.hrm_backend.entity.RefreshToken;
import com.phong.hrm_backend.entity.User;
import com.phong.hrm_backend.entity.UserRole;
import com.phong.hrm_backend.mapper.UserMapper;
import com.phong.hrm_backend.repository.RefreshTokenRepository;
import com.phong.hrm_backend.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.sql.Ref;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;


    @Transactional
    public UserResponseDTO register(RegisterRequest registerRequest) {
        if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()){
            throw new RuntimeException("Username is already in use");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .build();

        return userMapper.toUserResponseDTO(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest){
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new RuntimeException("Wrong password");
        }

        // xoa refresh token cu
        refreshTokenRepository.deleteByUser(user);

        String accessToken = jwtUtils.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .accessToken(accessToken)
                .build();

        return AuthResponse.builder()
                .userInfo(userResponseDTO)
                .jwtCookie(jwtUtils.createRefreshCookie(refreshToken))
                .build();
    }

    @Transactional
    public ResponseCookie logout(HttpServletRequest request, HttpServletResponse response){
        String refreshToken = jwtUtils.getJwtFromCookies(request);
        log.info("Refresh token: {}", refreshToken);



        if(refreshToken == null){
            throw new RuntimeException("Refresh token is null");
        }

        String username = jwtUtils.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken existedToken = refreshTokenRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Refresh token not found"));

        log.info("Existed Refresh token: {}", existedToken);
        refreshTokenRepository.delete(existedToken);

        ResponseCookie jwtCookie = jwtUtils.clearCookie();


        return  jwtCookie;
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken){
        log.info("Refresh token: {}", refreshToken);

        if(refreshToken == null){
            throw new RuntimeException("Refresh token is null");
        }


        if(!jwtUtils.validateToken(refreshToken)){
            throw new RuntimeException("Invalid refresh token");
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if(!refreshTokenEntity.getExpiresAt().isAfter(LocalDateTime.now())){
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new RuntimeException("Expired refresh token");
        }

        User user = refreshTokenEntity.getUser();

        String newAccessToken = jwtUtils.generateAccessToken(user.getUsername());

        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .accessToken(newAccessToken)
                .build();

        return AuthResponse.builder()
                .userInfo(userResponseDTO)
                .build();
    }

//    private AuthResponse generateAuthResponse(User user){
//        String accessToken = jwtUtils.generateAccessToken(user.getUsername());
//        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
//
//        RefreshToken refreshTokenEntity = RefreshToken.builder()
//                .token(refreshToken)
//                .user(user)
//                .expiresAt(LocalDateTime.now().plusDays(7))
//                .build();
//
//        refreshTokenRepository.save(refreshTokenEntity);
//
//        return AuthResponse.builder()
//                .accessToken(accessToken)
//                .username(user.getUsername())
//                .build();
//    }
}
