package com.fullstack.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos de una direccion de despacho del usuario")
public class UserAddressRequest {

    @Schema(description = "Nombre corto de la direccion", example = "Casa")
    @Size(max = 60, message = "El alias no puede superar 60 caracteres")
    private String alias;

    @Schema(description = "Region de Chile", example = "Metropolitana de Santiago")
    @NotBlank(message = "La region es obligatoria")
    @Size(max = 80, message = "La region no puede superar 80 caracteres")
    private String region;

    @Schema(description = "Comuna de la region", example = "Providencia")
    @NotBlank(message = "La comuna es obligatoria")
    @Size(max = 80, message = "La comuna no puede superar 80 caracteres")
    private String comuna;

    @Schema(description = "Calle o avenida", example = "Los Leones")
    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 120, message = "La calle no puede superar 120 caracteres")
    private String calle;

    @Schema(description = "Numero de calle", example = "123")
    @NotBlank(message = "El numero es obligatorio")
    @Pattern(regexp = "^[0-9]+[A-Za-z]?$", message = "El numero debe tener formato valido, por ejemplo 123 o 123B")
    private String numero;

    @Schema(description = "Detalle adicional opcional", example = "Depto 404")
    @Size(max = 200, message = "El detalle no puede superar 200 caracteres")
    private String detalle;

    @Schema(description = "Indica si debe quedar como direccion principal", example = "true")
    private boolean defaultAddress;
}
