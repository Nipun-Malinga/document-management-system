package com.nipun.system.auth;

import com.nipun.system.user.User;

public interface AuthService {
    User login(User user);
}
