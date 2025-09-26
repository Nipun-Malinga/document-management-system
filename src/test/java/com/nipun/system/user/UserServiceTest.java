package com.nipun.system.user;

import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUserShouldRegisterUserSuccessfully() {
        var user = new User();
        user.setUsername("Test User");
        user.setEmail("testuser@gmail.com");
        user.setPassword("test1234567");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        var savedUser =  userService.registerUser(user);

        assertNotNull(savedUser);
        assertEquals(savedUser, user);
        assertEquals(savedUser.getId(), user.getId());
    }

    @Test
    void registerUserShouldFailedIfEmailIsAlreadyRegistered() {
        var user = new User();
        user.setUsername("Test User");
        user.setEmail("testuser@gmail.com");
        user.setPassword("test1234567");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        EmailAlreadyRegisteredException exception = assertThrows(EmailAlreadyRegisteredException.class, () ->
                userService.registerUser(user)
        );

        assertEquals(
                "Email: " + user.getEmail()  + " is already registered in system.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }
}