package com.nipun.system.user.dtos;

import com.nipun.system.user.Role;
import lombok.Data;

@Data
public class UserResponse {

    private Long id;

    private String firstname;

    private String lastname;

    private String username;

    private String email;

    private Role role;
}
