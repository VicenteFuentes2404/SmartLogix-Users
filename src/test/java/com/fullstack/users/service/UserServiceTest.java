package com.fullstack.users.service;

import com.fullstack.users.dto.AuthResponse;
import com.fullstack.users.dto.LoginRequest;
import com.fullstack.users.dto.RegisterRequest;
import com.fullstack.users.dto.UpdatePhoneRequest;
import com.fullstack.users.dto.UserProfileResponse;
import com.fullstack.users.model.User;
import com.fullstack.users.model.UserRol;
import com.fullstack.users.model.UserStatus;
import com.fullstack.users.repository.UserRepository;
import com.fullstack.users.security.JwtService;
import com.fullstack.users.singleton.SessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registrar_whenEmailIsUnique_returnsAuthResponse() {
        // Arrange
        RegisterRequest request = registerRequest();
        when(userRepository.existsByEmail("cliente@smartlogix.cl")).thenReturn(false);
        when(passwordEncoder.encode("Cliente1234")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(sessionManager.createSession(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        // Act
        AuthResponse response = userService.registrar(request);

        // Assert
        assertEquals("cliente@smartlogix.cl", response.getEmail());
        assertEquals(UserRol.CLIENT, response.getRol());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registrar_whenEmailExists_throwsBadRequest() {
        // Arrange
        RegisterRequest request = registerRequest();
        when(userRepository.existsByEmail("cliente@smartlogix.cl")).thenReturn(true);

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.registrar(request)
        );

        // Assert
        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void login_whenCredentialsAreValid_returnsAuthResponse() {
        // Arrange
        LoginRequest request = loginRequest();
        User user = user();
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Cliente1234", "hash")).thenReturn(true);
        when(sessionManager.createSession(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        // Act
        AuthResponse response = userService.login(request);

        // Assert
        assertEquals("Login exitoso", response.getMessage());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void login_whenPasswordIsInvalid_throwsUnauthorized() {
        // Arrange
        LoginRequest request = loginRequest();
        User user = user();
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Cliente1234", "hash")).thenReturn(false);

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.login(request)
        );

        // Assert
        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void getAuthenticatedProfile_returnsProfileDtoWithoutPassword() {
        // Arrange
        User user = user();
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));

        // Act
        UserProfileResponse response = userService.getAuthenticatedProfile("cliente@smartlogix.cl");

        // Assert
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
    }

    @Test
    void updateAuthenticatedPhone_savesCleanPhone() {
        // Arrange
        User user = user();
        UpdatePhoneRequest request = new UpdatePhoneRequest();
        request.setPhone(" +56912345678 ");
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileResponse response = userService.updateAuthenticatedPhone("cliente@smartlogix.cl", request);

        // Assert
        assertEquals("+56912345678", response.getPhone());
        verify(userRepository).save(user);
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Cliente");
        request.setLastname("SmartLogix");
        request.setEmail(" Cliente@SmartLogix.cl ");
        request.setPassword("Cliente1234");
        request.setPhone("+56912345678");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail(" Cliente@SmartLogix.cl ");
        request.setPassword("Cliente1234");
        return request;
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Cliente")
                .lastname("SmartLogix")
                .email("cliente@smartlogix.cl")
                .password("hash")
                .phone("+56912345678")
                .rol(UserRol.CLIENT)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
