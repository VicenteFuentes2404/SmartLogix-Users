package com.fullstack.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request para editar el telefono del usuario autenticado")
public class UpdatePhoneRequest {

    @Schema(description = "Telefono chileno; puede venir vacio para limpiar el dato", example = "+56912345678")
    @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
    @Pattern(
            regexp = "^$|^(\\+56\\s?9\\s?\\d{4}\\s?\\d{4}|\\+569\\d{8}|9\\d{8})$",
            message = "El telefono debe ser chileno valido"
    )
    private String phone;
}
