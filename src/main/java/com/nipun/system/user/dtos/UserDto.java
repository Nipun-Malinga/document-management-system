package com.nipun.system.user.dtos;

import com.nipun.system.user.Role;
import lombok.Data;

@Data
public class UserDto {

    private Long id;

    private String username;

    private String email;

    private Role role;
}
