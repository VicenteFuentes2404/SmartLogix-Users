package com.fullstack.users.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fullstack.users.dto.AuthResponse;
import com.fullstack.users.dto.LoginRequest;
import com.fullstack.users.dto.RegisterRequest;
import com.fullstack.users.dto.UpdatePhoneRequest;
import com.fullstack.users.dto.UserProfileResponse;
import com.fullstack.users.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Registro, autenticacion, perfil y administracion de usuarios")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Registrar cliente", description = "Crea una cuenta CLIENT usando RegisterRequest y responde AuthResponse.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o email existente")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = userService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register-admin")
    @Operation(summary = "Registrar administrador", description = "Crea una cuenta ADMIN. Ruta protegida por el API Gateway.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Administrador registrado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN")
    })
    public ResponseEntity<AuthResponse> registerAdmin(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = userService.registrarAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica credenciales con LoginRequest y responde token en AuthResponse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil autenticado", description = "Retorna UserProfileResponse del usuario actual.")
    @ApiResponse(responseCode = "200", description = "Perfil encontrado")
    public ResponseEntity<UserProfileResponse> me(
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserProfileResponse response = userService.getAuthenticatedProfile(email);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/phone")
    @Operation(summary = "Actualizar telefono propio", description = "Permite al cliente editar su telefono usando UpdatePhoneRequest.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Telefono actualizado"),
            @ApiResponse(responseCode = "400", description = "Telefono invalido")
    })
    public ResponseEntity<UserProfileResponse> updateMyPhone(
            Authentication authentication,
            @Valid @RequestBody UpdatePhoneRequest request
    ) {
        return ResponseEntity.ok(userService.updateAuthenticatedPhone(authentication.getName(), request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesion", description = "Invalida la sesion asociada al token Bearer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesion cerrada"),
            @ApiResponse(responseCode = "400", description = "Header Authorization invalido")
    })
    public ResponseEntity<String> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        String token = extractToken(authorizationHeader);
        userService.logout(token);
        return ResponseEntity.ok("Sesion cerrada correctamente");
    }

    @GetMapping("/session-info")
    @Operation(summary = "Informacion de sesiones", description = "Retorna resumen del SessionManager singleton.")
    @ApiResponse(responseCode = "200", description = "Resumen de sesiones")
    public ResponseEntity<String> sessionInfo() {
        return ResponseEntity.ok(userService.sessionInfo());
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Operacion administrativa que retorna perfiles sin password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN")
    })
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok(
                userService.listUsers()
                        .stream()
                        .map(userService::toProfileResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Buscar usuario por ID", description = "Operacion administrativa que retorna UserProfileResponse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserProfileResponse> getUserById(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.toProfileResponse(userService.getUserById(userId)));
    }

    @PatchMapping("/{userId}/active")
    @Operation(summary = "Activar o desactivar usuario", description = "Operacion administrativa que cambia el estado activo del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserProfileResponse> updateActive(
            @PathVariable UUID userId,
            @RequestBody Map<String, Boolean> request
    ) {
        Boolean active = request.get("active");
        return ResponseEntity.ok(userService.toProfileResponse(userService.updateActive(userId, Boolean.TRUE.equals(active))));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Token invalido"
            );
        }

        return authorizationHeader.substring(7);
    }
}
