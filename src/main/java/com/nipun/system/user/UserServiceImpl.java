package com.nipun.system.user;

import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserResponse;
import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse registerUser(
            RegisterUserRequest request
    ) {
        if (request == null)
            throw new IllegalArgumentException("Register request cannot be null");

        var user = userMapper.toEntity(request);

        if (userRepository.existsByEmail(user.getEmail()))
            throw new EmailAlreadyRegisteredException("Email: " + user.getEmail() + " is already registered in system.");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public User findUser(Long userId) {

        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");

        var user = userRepository.findById(userId).orElse(null);

        if (user == null)
            throw new UserNotFoundException("User not found with user id: " + userId);

        return user;
    }

    @Cacheable(value = "users", key = "#userEmail")
    @Override
    public UserResponse findUser(String userEmail) {
        if (userEmail == null)
            throw new IllegalArgumentException("User email cannot be null");

        var user = userRepository.findByEmail(userEmail).orElse(null);

        if (user == null)
            throw new UserNotFoundException("User not found with user email: " + userEmail);

        return userMapper.toDto(user);
    }

}
