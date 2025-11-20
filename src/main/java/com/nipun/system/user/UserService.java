package com.nipun.system.user;

import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserResponse;


public interface UserService {
    UserResponse registerUser(RegisterUserRequest request);

    User findUser(Long id);

    UserResponse findUser(String email);
}
