package com.nipun.system.auth;

import com.nipun.system.auth.dtos.UserLoginRequest;
import com.nipun.system.shared.config.JwtConfig;
import com.nipun.system.shared.dtos.JwtResponseDto;
import com.nipun.system.shared.services.JwtService;
import com.nipun.system.user.UserMapper;
import com.nipun.system.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> userLogin(
            @RequestBody @Valid UserLoginRequest request,
            HttpServletResponse response
    ) {
        var user = authService.login(userMapper.toEntity(request));

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie =  new Cookie("refreshToken", refreshToken.toString());
        cookie.setMaxAge(Math.toIntExact(jwtConfig.getRefreshTokenExpiration()));
        cookie.setPath("/auth/refresh");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponseDto(accessToken.toString()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refresh(
            @CookieValue("refreshToken") String refreshToken
    ) {
        var jwt = jwtService.parseToken(refreshToken);

        if(jwt == null || jwt.isExpired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = userService.findUser(jwt.getUserId());
        return ResponseEntity.ok(new JwtResponseDto(jwtService.generateAccessToken(user).toString()));
    }
}
