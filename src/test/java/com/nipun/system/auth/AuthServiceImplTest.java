package com.nipun.system.auth;

import com.nipun.system.auth.dtos.UserLoginRequest;
import com.nipun.system.user.User;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Test")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserLoginRequest testUserLoginRequest;
    private User testUser;

    @BeforeEach
    void setup() {
        testUserLoginRequest = new UserLoginRequest();
        testUserLoginRequest.setEmail("testuser@email.com");
        testUserLoginRequest.setPassword("test12345");

        this.testUser = new User();
        this.testUser.setUsername("Test User");
        this.testUser.setEmail("testuser@email.com");
        this.testUser.setPassword("test12345");
    }

    @Nested
    @DisplayName("User Login Tests")
    class UserLoginTest {

        @Test
        @DisplayName("Should return a user for valid login request")
        void userLoginTest() {
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

            var user = AuthServiceImplTest.this.authService.login(testUserLoginRequest);

            assertEquals(user.getEmail(), testUser.getEmail());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        }

        @Test
        @DisplayName("Should propagate exception when authentication fails")
        void shouldPropagateAuthenticationFailure() {
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> AuthServiceImplTest.this.authService.login(testUserLoginRequest)
            );

            assertEquals("Bad credentials", exception.getMessage());

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException for invalid user email")
        void shouldThrowUserNotFoundException() {
            when(userRepository.findByEmail(testUserLoginRequest.getEmail())).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> AuthServiceImplTest.this.authService.login(testUserLoginRequest)
            );

            assertEquals(
                    "User not found for user email: " + testUserLoginRequest.getEmail(),
                    exception.getMessage()
            );

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, times(1)).findByEmail(testUserLoginRequest.getEmail());
        }

        @Test
        @DisplayName("Should handle null user login request")
        void shouldHandleNullUserLoginRequest() {

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> AuthServiceImplTest.this.authService.login(null)
            );

            assertEquals(
                    "User login request cannot be null",
                    exception.getMessage()
            );

            verifyNoInteractions(authenticationManager);
            verifyNoInteractions(userRepository);
        }
    }
}