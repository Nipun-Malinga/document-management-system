package com.nipun.system.user;

import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserDto;


public interface UserService {
    UserDto registerUser(RegisterUserRequest request);

    User findUser(Long id);

    UserDto findUser(String email);
}
