package com.nipun.system.user;

import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public User registerUser(
            User user
    ) {
        if(userRepository.existsByEmail(user.getEmail()))
            throw new EmailAlreadyRegisteredException(
                    "Email: " + user.getEmail()  + " is already registered in system."
            );

        user.setRole(Role.USER);
        userRepository.save(user);

        return user;
    }

}
