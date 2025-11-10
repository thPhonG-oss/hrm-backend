package com.phong.hrm_backend.service;

import com.phong.hrm_backend.dto.request.RegisterRequest;
import com.phong.hrm_backend.dto.response.UserResponseDTO;

public interface UserService {

    UserResponseDTO createNewUser(RegisterRequest request) throws Exception;

    UserResponseDTO getUserById(Long userId);
}
