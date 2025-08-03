package com.nipun.system.user;

import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(
            User user
    ) {
        if(userRepository.existsByEmail(user.getEmail()))
            throw new EmailAlreadyRegisteredException(
                    "Email: " + user.getEmail()  + " is already registered in system."
            );

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return user;
    }

    public User findUser(Long id) {
        var user = userRepository.findById(id).orElse(null);

        if(user == null)
            throw new UserNotFoundException();

        return user;
    }

}
