package com.nipun.system.user;

import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserDto;
import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import com.nipun.system.user.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Test")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterUserRequest testRegisterUserRequest;
    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setup() {
        this.testRegisterUserRequest = new RegisterUserRequest();
        this.testRegisterUserRequest.setUsername("Test User");
        this.testRegisterUserRequest.setEmail("testuser@email.com");
        this.testRegisterUserRequest.setPassword("test12345");

        this.testUser = new User();
        this.testUser.setUsername("Test User");
        this.testUser.setEmail("testuser@email.com");
        this.testUser.setPassword("test12345");

        this.testUserDto = new UserDto();
        this.testUserDto.setId(1L);
        this.testUserDto.setUsername("Test User");
        this.testUserDto.setEmail("testuser@email.com");
        this.testUserDto.setRole(Role.USER);
    }

    @Nested
    @DisplayName("Register User Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("Should register a user when valid request data exists")
        void shouldRegisterUserSuccessfully() {
            when(userMapper.toEntity(testRegisterUserRequest)).thenReturn(testUser);
            when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

            var result = UserServiceImplTest.this.userService.registerUser(testRegisterUserRequest);

            assertNotNull(result);
            assertEquals(testUserDto, result);
            verify(userMapper, times(1)).toEntity(testRegisterUserRequest);
            verify(userRepository, times(1)).existsByEmail(testUser.getEmail());
            verify(userRepository, times(1)).save(any(User.class));
            verify(userMapper, times(1)).toDto(any(User.class));

            verify(userRepository).save(
                    argThat(user -> user.getEmail().equals("testuser@email.com"))
            );
        }

        @Test
        @DisplayName("Should throw EmailAlreadyRegisteredException when email already exists")
        void shouldThrowEmailAlreadyRegisteredException() {
            when(userMapper.toEntity(testRegisterUserRequest)).thenReturn(testUser);
            when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

            EmailAlreadyRegisteredException exception = assertThrows(
                    EmailAlreadyRegisteredException.class,
                    () -> UserServiceImplTest.this.userService.registerUser(testRegisterUserRequest)
            );

            assertEquals(
                    "Email: " + testUser.getEmail() + " is already registered in system.",
                    exception.getMessage()
            );

            verify(userRepository, times(1)).existsByEmail(testUser.getEmail());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle null register user request ")
        void shouldHandleNullRegisterUserRequest() {
            testRegisterUserRequest = null;

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> UserServiceImplTest.this.userService.registerUser(testRegisterUserRequest)
            );

            assertEquals(
                    "Register request cannot be null",
                    exception.getMessage()
            );

            verifyNoInteractions(userMapper);
            verifyNoInteractions(userRepository);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Find User Tests Using User ID")
    class FindUserTestsUsingUserID {

        @Test
        @DisplayName("Should find user with valid user id")
        void shouldFindUserWithValidUserId() {
            var userId = 1L;

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            var user = UserServiceImplTest.this.userService.findUser(userId);

            assertEquals(user.getEmail(), testUser.getEmail());

            verify(userRepository, times(1)).findById(userId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException for unregistered user")
        void shouldThrowUserNotFoundException() {
            var userId = 1L;

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> UserServiceImplTest.this.userService.findUser(userId)
            );

            assertEquals(
                    "User not found with user id: " + userId,
                    exception.getMessage()
            );

            verify(userRepository, times(1)).findById(userId);
        }

        @Test
        @DisplayName("Should handle null user ID request")
        void shouldHandleNullUserIdRequest() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> UserServiceImplTest.this.userService.findUser((Long) null)
            );

            assertEquals(
                    "User ID cannot be null", exception.getMessage()
            );

            verifyNoInteractions(userRepository);
        }
    }

    @Nested
    @DisplayName("Find User Tests Using User Email")
    class FindUserTestsUsingUserEmail {

        @Test
        @DisplayName("Should find user with valid user email")
        void shouldFindUserWithValidUserEmail() {
            var userEmail = "testuser@email.com";

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
            when(userMapper.toDto(testUser)).thenReturn(testUserDto);

            var userDto = UserServiceImplTest.this.userService.findUser(userEmail);

            assertEquals(userDto, testUserDto);
            assertEquals(userEmail, userDto.getEmail());

            verify(userRepository, times(1)).findByEmail(userEmail);
            verify(userMapper, times(1)).toDto(testUser);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException for unregistered user")
        void shouldThrowUserNotFoundException() {
            var userEmail = "testUser@email.com";

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> UserServiceImplTest.this.userService.findUser(userEmail)
            );

            assertEquals(
                    "User not found with user email: " + userEmail,
                    exception.getMessage()
            );

            verify(userRepository, times(1)).findByEmail(userEmail);
        }

        @Test
        @DisplayName("Should handle null user email request")
        void shouldHandleNullUserEmailRequest() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> UserServiceImplTest.this.userService.findUser((String) null)
            );

            assertEquals(
                    "User email cannot be null", exception.getMessage()
            );

            verifyNoInteractions(userRepository);
        }
    }
}