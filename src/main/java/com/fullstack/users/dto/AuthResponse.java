package com.fullstack.users.dto;

import java.util.UUID;

import com.fullstack.users.model.UserRol;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de autenticacion con datos publicos del usuario y token JWT")
public class AuthResponse {

    @Schema(description = "ID del usuario", example = "5fb3a5dc-7f1f-4efa-98ee-7bcb0d2fce39")
    private UUID id;
    @Schema(description = "Nombre del usuario", example = "Vicente")
    private String name;
    @Schema(description = "Apellido del usuario", example = "Perez")
    private String lastname;
    @Schema(description = "Correo del usuario", example = "cliente@smartlogix.cl")
    private String email;
    @Schema(description = "Rol asignado", example = "CLIENT")
    private UserRol rol;

    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    @Schema(description = "Tipo de token", example = "Bearer")
    private String tokenType;
    @Schema(description = "Duracion del token en milisegundos", example = "3600000")
    private Long expiresIn;

    @Schema(description = "Mensaje de resultado", example = "Login exitoso")
    private String message;
}
