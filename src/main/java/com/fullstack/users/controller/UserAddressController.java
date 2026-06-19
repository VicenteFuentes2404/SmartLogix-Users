package com.fullstack.users.controller;

import com.fullstack.users.dto.UserAddressRequest;
import com.fullstack.users.dto.UserAddressResponse;
import com.fullstack.users.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
@Tag(name = "User Addresses", description = "Direcciones de despacho del usuario autenticado")
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    @Operation(summary = "Listar mis direcciones", description = "Retorna direcciones del usuario autenticado como UserAddressResponse.")
    @ApiResponse(responseCode = "200", description = "Direcciones encontradas")
    public ResponseEntity<List<UserAddressResponse>> listMine(Authentication authentication) {
        return ResponseEntity.ok(userAddressService.listMine(authentication.getName()));
    }

    @PostMapping
    @Operation(summary = "Crear direccion", description = "Crea una direccion usando UserAddressRequest.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Direccion creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<UserAddressResponse> create(
            Authentication authentication,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userAddressService.create(authentication.getName(), request));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Editar direccion", description = "Actualiza una direccion propia usando UserAddressRequest.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Direccion actualizada"),
            @ApiResponse(responseCode = "404", description = "Direccion no encontrada")
    })
    public ResponseEntity<UserAddressResponse> update(
            Authentication authentication,
            @PathVariable UUID addressId,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return ResponseEntity.ok(userAddressService.update(authentication.getName(), addressId, request));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Eliminar direccion", description = "Elimina una direccion propia y reasigna default si corresponde.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Direccion eliminada"),
            @ApiResponse(responseCode = "404", description = "Direccion no encontrada")
    })
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable UUID addressId
    ) {
        userAddressService.delete(authentication.getName(), addressId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    @Operation(summary = "Marcar direccion por defecto", description = "Define una direccion propia como default.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Direccion marcada como default"),
            @ApiResponse(responseCode = "404", description = "Direccion no encontrada")
    })
    public ResponseEntity<UserAddressResponse> setDefault(
            Authentication authentication,
            @PathVariable UUID addressId
    ) {
        return ResponseEntity.ok(userAddressService.setDefault(authentication.getName(), addressId));
    }
}
