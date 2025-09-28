package com.nipun.system.auth;

import com.nipun.system.auth.dtos.UserLoginRequest;
import com.nipun.system.shared.config.JwtConfig;
import com.nipun.system.shared.dtos.JwtResponseDto;
import com.nipun.system.shared.services.JwtService;
import com.nipun.system.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth Controller", description = "Manage authentication in the system")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<JwtResponseDto> userLogin(
            @RequestBody @Valid UserLoginRequest request,
            HttpServletResponse response
    ) {
        var user = authService.login(request);

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setMaxAge(Math.toIntExact(jwtConfig.getRefreshTokenExpiration()));
        cookie.setPath("/auth/refresh");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponseDto(accessToken.toString()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "JWT token refresh", description = "Refresh the access token")
    public ResponseEntity<JwtResponseDto> refresh(
            @CookieValue("refreshToken")
            @Parameter(description = "Refresh token issued by the server when login")
            String refreshToken
    ) {
        var jwt = jwtService.parseToken(refreshToken);

        if (jwt == null || jwt.isExpired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = userService.findUser(jwt.getUserId());
        return ResponseEntity.ok(new JwtResponseDto(jwtService.generateAccessToken(user).toString()));
    }
}
