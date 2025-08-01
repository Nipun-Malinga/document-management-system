package com.nipun.system.auth;

import com.nipun.system.user.User;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    public void login(User user) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        user.getPassword()
                )
        );
    }
}
