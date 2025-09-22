package com.nipun.system.user.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for register user")
public class RegisterUserRequest {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, max = 20, message = "Username must have characters between 4 and 20")
    @Schema(description = "Username", example = "John Doe")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Schema(description = "User email", example = "johndoe@email.com")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 20, message = "Password must have characters between 8 and 20")
    @Schema(description = "User password", example = "johndoe1234")
    private String password;
}
