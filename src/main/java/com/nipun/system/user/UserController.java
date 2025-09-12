package com.nipun.system.user;

import com.nipun.system.shared.dtos.ErrorResponse;
import com.nipun.system.user.dtos.FindUserRequest;
import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserDto;
import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(
            @RequestBody @Valid RegisterUserRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var userDto = userService.registerUser(request);

        var uri = uriBuilder
                    .path("/users/{userId}")
                    .buildAndExpand(userDto.getId())
                    .toUri();

        return ResponseEntity.created(uri).body(userDto);
    }

    @GetMapping("/find")
    public ResponseEntity<UserDto> findUser(
            @RequestBody @Valid FindUserRequest request
    ) {
        var userDto = userService.findUser(request.getEmail());

        return ResponseEntity.ok(userDto);
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
