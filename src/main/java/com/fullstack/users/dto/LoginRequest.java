package com.fullstack.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciales de acceso del usuario")
public class LoginRequest {

    @Schema(description = "Correo registrado", example = "cliente@smartlogix.cl")
    @Email(message = "El correo debe tener un formato valido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @Schema(description = "Contrasena de la cuenta", example = "Cliente1234")
    @NotBlank(message = "La contrasena es obligatoria")
    private String password;
}
