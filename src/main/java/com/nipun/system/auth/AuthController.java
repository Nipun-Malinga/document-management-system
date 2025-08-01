package com.nipun.system.auth;

import com.nipun.system.auth.dtos.UserLoginRequest;
import com.nipun.system.user.UserMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> userLogin(
            @RequestBody @Valid UserLoginRequest request
    ) {
        authService.login(userMapper.toEntity(request));
        return ResponseEntity.ok().build();
    }
}
