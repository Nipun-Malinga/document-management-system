package com.nipun.system.user;

import com.nipun.system.auth.dtos.UserLoginRequest;
import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);
    User toEntity(UserLoginRequest request);
}
