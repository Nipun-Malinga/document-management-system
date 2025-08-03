package com.nipun.system.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nipun.system.shared.config.SecurityConfig;
import com.nipun.system.shared.exceptions.GlobalExceptionHandler;
import com.nipun.system.shared.filters.JwtAuthenticationFilter;
import com.nipun.system.shared.services.JwtService;
import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.dtos.UserDto;
import com.nipun.system.user.exceptions.EmailAlreadyRegisteredException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void userRegisterShouldReturnNewUser() throws Exception {
        var request = new RegisterUserRequest(
                "Test User",
                "testuser@gmail.com",
                "test1234567"
        );

        var user = new User();
        user.setId(1L);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        var userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername(request.getUsername());
        userDto.setEmail(request.getEmail());
        userDto.setRole(Role.USER);

        var jwt = jwtService.generateAccessToken(user);

        when(userMapper.toEntity(any(RegisterUserRequest.class))).thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userService.registerUser(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value(request.getUsername()))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void userRegisterShouldFailIfEmailIsAlreadyRegistered() throws Exception {
        var request = new RegisterUserRequest(
                "Test User",
                "nipunmalinga11@gmail.com",
                "test1234567"
        );

        var user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(Role.USER);

        var jwt = jwtService.generateAccessToken(user);

        when(userMapper.toEntity(any(RegisterUserRequest.class))).thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
        when(userService.registerUser(user))
                .thenThrow(new EmailAlreadyRegisteredException(
                        "Email: " + user.getEmail() + " is already registered in system."
                ));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Email: " + user.getEmail()  + " is already registered in system."));
    }

    @Test
    void userRegisterFailIfUserNameIsBlank() throws Exception {
        var request = new RegisterUserRequest();
        request.setEmail("testuser@gmail.com");
        request.setPassword("test1234567");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.username").value("Username cannot be blank"));
    }

    @Test
    void userRegisterFailIfUserNameSizeLessThanMinSize() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("Te");
        request.setEmail("testuser@gmail.com");
        request.setPassword("test1234567");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.username")
                        .value("Username must have characters between 4 and 20"));
    }

    @Test
    void userRegisterFailIfUserNameSizeGreaterThanMaxSize() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("TestUserTestUserTestUser");
        request.setEmail("testuser@gmail.com");
        request.setPassword("test1234567");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.username")
                        .value("Username must have characters between 4 and 20"));
    }

    @Test
    void userRegisterFailIfEmailIsBlank() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("Test User V2");
        request.setPassword("test1234567");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.email")
                        .value("Email cannot be blank"));
    }

    @Test
    void userRegisterFailIfEmailIsInvalid() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("Test User V2");
        request.setEmail("testusergmail.com");
        request.setPassword("test1234567");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.email")
                        .value("Invalid email format"));
    }

    @Test
    void userRegisterFailIfPasswordIsBlank() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("Test User V2");
        request.setEmail("testuser@gmail.com");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.password")
                        .value("Password cannot be blank"));
    }

    @Test
    void userRegisterFailIfPasswordSizeLessThanMinSize() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("Test User V2");
        request.setEmail("testuser@gmail.com");
        request.setPassword("test");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.password")
                        .value("Password must have characters between 8 and 20"));
    }

    @Test
    void userRegisterFailIfPasswordSizeGreaterThanMaxSize() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("Test User V2");
        request.setEmail("testuser@gmail.com");
        request.setPassword("testusertestusertestuser");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.password")
                        .value("Password must have characters between 8 and 20"));
    }
}