package com.phong.hrm_backend.service.impl;

import com.phong.hrm_backend.dto.request.RegisterRequest;
import com.phong.hrm_backend.dto.response.UserResponseDTO;
import com.phong.hrm_backend.entity.User;
import com.phong.hrm_backend.entity.UserRole;
import com.phong.hrm_backend.mapper.UserMapper;
import com.phong.hrm_backend.repository.UserRepository;
import com.phong.hrm_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO createNewUser(RegisterRequest request) throws Exception {

        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("Username is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();

        return userMapper.toUserResponseDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO getUserById(Long userId){

        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));

        return userMapper.toUserResponseDTO(user);
    }
}
