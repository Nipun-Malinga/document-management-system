package com.nipun.system.auth;

import com.nipun.system.auth.dtos.UserLoginRequest;
import com.nipun.system.shared.config.JwtConfig;
import com.nipun.system.shared.dtos.JwtResponseDto;
import com.nipun.system.shared.services.JwtService;
import com.nipun.system.user.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> userLogin(
            @RequestBody @Valid UserLoginRequest request,
            HttpServletResponse response
    ) {
        var user = authService.login(userMapper.toEntity(request));

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie =  new Cookie("authorization", refreshToken.toString());
        cookie.setMaxAge(Math.toIntExact(jwtConfig.getRefreshTokenExpiration()));
        cookie.setPath("/auth/refresh");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponseDto(accessToken.toString()));
    }
}
