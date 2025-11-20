package com.nipun.system.user;

import com.nipun.system.auth.dtos.UserLoginRequest;
import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);

    User toEntity(RegisterUserRequest request);

    User toEntity(UserLoginRequest request);
}
