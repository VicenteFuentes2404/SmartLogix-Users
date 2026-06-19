package com.fullstack.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar una cuenta cliente en SmartLogix")
public class RegisterRequest {

    @Schema(description = "Nombre del usuario", example = "Vicente")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El nombre solo puede contener letras y espacios")
    private String name;

    @Schema(description = "Apellido del usuario", example = "Perez")
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 80, message = "El apellido debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El apellido solo puede contener letras y espacios")
    private String lastname;

    @Schema(description = "Correo unico de la cuenta", example = "cliente@smartlogix.cl")
    @Email(message = "El correo debe tener un formato valido")
    @NotBlank(message = "El correo es obligatorio")
    @Size(max = 120, message = "El correo no puede superar 120 caracteres")
    private String email;

    @Schema(description = "Contrasena con mayuscula, minuscula y numero", example = "Cliente1234")
    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "La contrasena debe incluir mayuscula, minuscula y numero")
    private String password;

    @Schema(description = "Telefono chileno opcional", example = "+56912345678")
    @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
    @Pattern(regexp = "^$|^(\\+56\\s?9\\s?\\d{4}\\s?\\d{4}|\\+569\\d{8}|9\\d{8})$", message = "El telefono debe ser chileno valido")
    private String phone;

    @Schema(description = "Direccion simple heredada para compatibilidad", example = "Los Leones 123")
    @Size(max = 200, message = "La direccion no puede superar 200 caracteres")
    private String address;
}
