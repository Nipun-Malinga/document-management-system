package com.nipun.system.user;

import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserDto;
import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserDto registerUser(
            RegisterUserRequest request
    ) {
        var user = userMapper.toEntity(request);

        if(userRepository.existsByEmail(user.getEmail()))
            throw new EmailAlreadyRegisteredException(
                    "Email: " + user.getEmail()  + " is already registered in system."
            );

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        return userMapper.toDto(userRepository.save(user));
    }

    public User findUser(Long id) {
        var user = userRepository.findById(id).orElse(null);

        if(user == null)
            throw new UserNotFoundException();

        return user;
    }

    @Cacheable(value = "users", key = "#email")
    public UserDto findUser(String email) {
        var user = userRepository.findByEmail(email).orElse(null);

        if (user == null)
            throw new UserNotFoundException();

        return userMapper.toDto(user);
    }

}
