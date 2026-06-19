package com.fullstack.users.dto;

import java.util.UUID;

import com.fullstack.users.model.UserRol;
import com.fullstack.users.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//este DTO se utiliza para enviar la información del perfil del usuario al cliente, incluyendo su rol y estado, pero sin exponer información sensible como la contraseña.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Perfil publico del usuario sin exponer password")
public class UserProfileResponse {

    @Schema(description = "ID del usuario", example = "5fb3a5dc-7f1f-4efa-98ee-7bcb0d2fce39")
    private UUID id;
    @Schema(description = "Nombre", example = "Vicente")
    private String name;
    @Schema(description = "Apellido", example = "Perez")
    private String lastname;
    @Schema(description = "Correo", example = "cliente@smartlogix.cl")
    private String email;
    @Schema(description = "Telefono", example = "+56912345678")
    private String phone;
    @Schema(description = "Direccion legacy de compatibilidad", example = "Los Leones 123")
    private String address;
    @Schema(description = "Rol de sistema", example = "CLIENT")
    private UserRol rol;
    @Schema(description = "Estado de la cuenta", example = "ACTIVE")
    private UserStatus status;
}
