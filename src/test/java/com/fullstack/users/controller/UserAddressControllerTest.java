package com.fullstack.users.controller;

import com.fullstack.users.dto.UserAddressResponse;
import com.fullstack.users.service.UserAddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserAddressControllerTest {

    @Mock
    private UserAddressService userAddressService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserAddressController(userAddressService)).build();
    }

    @Test
    void listMine_returnsUserAddressResponseDtos() throws Exception {
        // Arrange
        UserAddressResponse response = addressResponse(true);
        when(userAddressService.listMine("cliente@smartlogix.cl")).thenReturn(List.of(response));

        // Act
        var result = mockMvc.perform(get("/api/users/me/addresses")
                .principal(authentication()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].region").value("Metropolitana de Santiago"))
                .andExpect(jsonPath("$[0].defaultAddress").value(true));
    }

    @Test
    void create_returnsCreatedUserAddressResponseDto() throws Exception {
        // Arrange
        UserAddressResponse response = addressResponse(true);
        when(userAddressService.create(eq("cliente@smartlogix.cl"), any())).thenReturn(response);

        // Act
        var result = mockMvc.perform(post("/api/users/me/addresses")
                .principal(authentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addressJson(true)));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.alias").value("Casa"))
                .andExpect(jsonPath("$.comuna").value("Providencia"));
        verify(userAddressService).create(eq("cliente@smartlogix.cl"), any());
    }

    @Test
    void update_returnsUpdatedUserAddressResponseDto() throws Exception {
        // Arrange
        UUID addressId = UUID.randomUUID();
        UserAddressResponse response = addressResponse(false);
        when(userAddressService.update(eq("cliente@smartlogix.cl"), eq(addressId), any())).thenReturn(response);

        // Act
        var result = mockMvc.perform(put("/api/users/me/addresses/{addressId}", addressId)
                .principal(authentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addressJson(false)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.defaultAddress").value(false));
        verify(userAddressService).update(eq("cliente@smartlogix.cl"), eq(addressId), any());
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        // Arrange
        UUID addressId = UUID.randomUUID();

        // Act
        var result = mockMvc.perform(delete("/api/users/me/addresses/{addressId}", addressId)
                .principal(authentication()));

        // Assert
        result.andExpect(status().isNoContent());
        verify(userAddressService).delete("cliente@smartlogix.cl", addressId);
    }

    @Test
    void setDefault_returnsUpdatedUserAddressResponseDto() throws Exception {
        // Arrange
        UUID addressId = UUID.randomUUID();
        UserAddressResponse response = addressResponse(true);
        when(userAddressService.setDefault("cliente@smartlogix.cl", addressId)).thenReturn(response);

        // Act
        var result = mockMvc.perform(patch("/api/users/me/addresses/{addressId}/default", addressId)
                .principal(authentication()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultAddress").value(true));
        verify(userAddressService).setDefault("cliente@smartlogix.cl", addressId);
    }

    private String addressJson(boolean defaultAddress) {
        return """
                {
                  "alias": "Casa",
                  "region": "Metropolitana de Santiago",
                  "comuna": "Providencia",
                  "calle": "Los Leones",
                  "numero": "123",
                  "detalle": "Depto 404",
                  "defaultAddress": %s
                }
                """.formatted(defaultAddress);
    }

    private UserAddressResponse addressResponse(boolean defaultAddress) {
        return UserAddressResponse.builder()
                .id(UUID.randomUUID())
                .alias("Casa")
                .region("Metropolitana de Santiago")
                .comuna("Providencia")
                .calle("Los Leones")
                .numero("123")
                .detalle("Depto 404")
                .defaultAddress(defaultAddress)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("cliente@smartlogix.cl", "N/A", List.of());
    }
}
