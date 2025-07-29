package com.nipun.system.user;

import com.nipun.system.shared.dtos.ErrorResponse;
import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserDto;
import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(
            @RequestBody @Valid RegisterUserRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var user = userService
                        .registerUser(userMapper.toEntity(request));

        var uri = uriBuilder
                    .path("/users/{userId}")
                    .buildAndExpand(user.getId())
                    .toUri();

        return ResponseEntity.created(uri).body(userMapper.toDto(user));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyRegistered(
            EmailAlreadyRegisteredException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(exception.getMessage()));
    }
}
