package com.fullstack.users.dto;

import com.fullstack.users.model.UserAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Direccion de despacho retornada al cliente")
public class UserAddressResponse {

    @Schema(description = "ID de direccion", example = "7dfddf73-3ea5-4dd3-9f9a-5ef0e3e51b13")
    private UUID id;
    @Schema(description = "Alias de direccion", example = "Casa")
    private String alias;
    @Schema(description = "Region", example = "Metropolitana de Santiago")
    private String region;
    @Schema(description = "Comuna", example = "Providencia")
    private String comuna;
    private String calle;
    private String numero;
    private String detalle;
    private boolean defaultAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserAddressResponse fromEntity(UserAddress address) {
        return UserAddressResponse.builder()
                .id(address.getId())
                .alias(address.getAlias())
                .region(address.getRegion())
                .comuna(address.getComuna())
                .calle(address.getCalle())
                .numero(address.getNumero())
                .detalle(address.getDetalle())
                .defaultAddress(address.isDefaultAddress())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
