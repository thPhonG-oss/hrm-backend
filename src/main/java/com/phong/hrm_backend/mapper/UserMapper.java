package com.phong.hrm_backend.mapper;

import com.phong.hrm_backend.dto.response.UserResponseDTO;
import com.phong.hrm_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserResponseDTO userResponseDTO);
    UserResponseDTO toUserResponseDTO(User user);
}
