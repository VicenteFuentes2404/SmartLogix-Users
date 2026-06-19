package com.fullstack.users.controller;

import com.fullstack.users.dto.AuthResponse;
import com.fullstack.users.dto.UpdatePhoneRequest;
import com.fullstack.users.dto.UserProfileResponse;
import com.fullstack.users.model.UserRol;
import com.fullstack.users.model.UserStatus;
import com.fullstack.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
    }

    @Test
    void register_returnsAuthResponseDto() throws Exception {
        // Arrange
        AuthResponse response = authResponse("Registro exitoso");
        when(userService.registrar(any())).thenReturn(response);
        String json = """
                {
                  "name": "Cliente",
                  "lastname": "SmartLogix",
                  "email": "cliente@smartlogix.cl",
                  "password": "Cliente1234",
                  "phone": "+56912345678"
                }
                """;

        // Act
        var result = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("cliente@smartlogix.cl"))
                .andExpect(jsonPath("$.rol").value("CLIENT"))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$", not(hasKey("password"))));
        verify(userService).registrar(any());
    }

    @Test
    void login_returnsAuthResponseDto() throws Exception {
        // Arrange
        AuthResponse response = authResponse("Login exitoso");
        when(userService.login(any())).thenReturn(response);
        String json = """
                {
                  "email": "cliente@smartlogix.cl",
                  "password": "Cliente1234"
                }
                """;

        // Act
        var result = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login exitoso"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        verify(userService).login(any());
    }

    @Test
    void me_returnsUserProfileResponseDto() throws Exception {
        // Arrange
        UserProfileResponse response = profileResponse();
        when(userService.getAuthenticatedProfile("cliente@smartlogix.cl")).thenReturn(response);

        // Act
        var result = mockMvc.perform(get("/api/users/me")
                .principal(authentication()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cliente@smartlogix.cl"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$", not(hasKey("password"))));
    }

    @Test
    void updateMyPhone_returnsUpdatedUserProfileResponseDto() throws Exception {
        // Arrange
        UserProfileResponse response = profileResponse();
        response.setPhone("+56987654321");
        when(userService.updateAuthenticatedPhone(eq("cliente@smartlogix.cl"), any(UpdatePhoneRequest.class)))
                .thenReturn(response);
        String json = """
                {
                  "phone": "+56987654321"
                }
                """;

        // Act
        var result = mockMvc.perform(patch("/api/users/me/phone")
                .principal(authentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+56987654321"));
        verify(userService).updateAuthenticatedPhone(eq("cliente@smartlogix.cl"), any(UpdatePhoneRequest.class));
    }

    @Test
    void listUsers_returnsProfileResponseDtos() throws Exception {
        // Arrange
        when(userService.listUsers()).thenReturn(List.of());

        // Act
        var result = mockMvc.perform(get("/api/users"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private AuthResponse authResponse(String message) {
        return AuthResponse.builder()
                .id(UUID.randomUUID())
                .name("Cliente")
                .lastname("SmartLogix")
                .email("cliente@smartlogix.cl")
                .rol(UserRol.CLIENT)
                .token("jwt-token")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .message(message)
                .build();
    }

    private UserProfileResponse profileResponse() {
        return UserProfileResponse.builder()
                .id(UUID.randomUUID())
                .name("Cliente")
                .lastname("SmartLogix")
                .email("cliente@smartlogix.cl")
                .phone("+56912345678")
                .address("Los Leones 123")
                .rol(UserRol.CLIENT)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("cliente@smartlogix.cl", "N/A", List.of());
    }
}
