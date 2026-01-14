package com.nipun.system.user;

import com.nipun.system.shared.dtos.ErrorResponse;
import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserResponse;
import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Manage users in the system")
public class UserController {
    private final UserService userService;

    @RateLimiter(name = "globalLimiter")
    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register new user to the system")
    public ResponseEntity<UserResponse> registerUser(
            @RequestBody @Valid RegisterUserRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var userDto = userService.registerUser(request, Role.USER);

        var uri = uriBuilder
                .path("/users/{userId}")
                .buildAndExpand(userDto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(userDto);
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/find/{email}")
    @Operation(summary = "Find user", description = "Find user based by email")
    public ResponseEntity<UserResponse> findUser(
            @PathVariable(name = "email")
            @Parameter(description = "User email", example = "johndoe@email.com")
            String email
    ) {
        var userDto = userService.findUser(email);

        return ResponseEntity.ok(userDto);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyRegistered(
            EmailAlreadyRegisteredException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(exception.getMessage()));

    }
}
