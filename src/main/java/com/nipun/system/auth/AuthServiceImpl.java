package com.nipun.system.auth;

import com.nipun.system.auth.dtos.UserLoginRequest;
import com.nipun.system.user.User;
import com.nipun.system.user.UserMapper;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User login(UserLoginRequest request) {

        var user = userMapper.toEntity(request);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        user.getPassword()
                )
        );

        return userRepository.findByEmail(user.getEmail()).orElseThrow();
    }
}
