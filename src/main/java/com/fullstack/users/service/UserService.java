package com.fullstack.users.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fullstack.users.dto.AuthResponse;
import com.fullstack.users.dto.LoginRequest;
import com.fullstack.users.dto.RegisterRequest;
import com.fullstack.users.dto.UpdatePhoneRequest;
import com.fullstack.users.dto.UserProfileResponse;
import com.fullstack.users.model.User;
import com.fullstack.users.model.UserRol;
import com.fullstack.users.model.UserStatus;
import com.fullstack.users.repository.UserRepository;
import com.fullstack.users.security.JwtService;
import com.fullstack.users.singleton.SessionManager;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionManager sessionManager;
    private final JwtService jwtService;

    public AuthResponse registrar(RegisterRequest request) {
        User savedUser = createUser(request, UserRol.CLIENT);
        String token = sessionManager.createSession(savedUser);

        return buildAuthResponse(
                savedUser,
                token,
                "Usuario registrado correctamente"
        );
    }

    public AuthResponse registrarAdmin(RegisterRequest request) {
        if (userRepository.existsByRol(UserRol.ADMIN)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Ya existe un usuario administrador"
            );
        }

        User savedUser = createUser(request, UserRol.ADMIN);
        String token = sessionManager.createSession(savedUser);

        return buildAuthResponse(
                savedUser,
                token,
                "Administrador registrado correctamente"
        );
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales invalidas"
                ));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario inactivo o bloqueado"
            );
        }

        boolean passwordValid = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordValid) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales invalidas"
            );
        }

        String token = sessionManager.createSession(user);

        return buildAuthResponse(
                user,
                token,
                "Login exitoso"
        );
    }

    public UserProfileResponse getAuthenticatedProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        return toProfileResponse(user);
    }

    public UserProfileResponse updateAuthenticatedPhone(String email, UpdatePhoneRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        user.setPhone(cleanNullable(request.getPhone()));
        return toProfileResponse(userRepository.save(user));
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
    }

    public User updateActive(UUID userId, boolean active) {
        User user = getUserById(userId);
        user.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        return userRepository.save(user);
    }

    public UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .rol(user.getRol())
                .status(user.getStatus())
                .build();
    }

    public void logout(String token) {
        sessionManager.closeSession(token);
    }

    public String sessionInfo() {
        return "SessionManager Singleton activo"
                + "\nHash instancia: " + sessionManager.getInstanceHashCode()
                + "\nSesiones activas: " + sessionManager.getActiveSessionsCount()
                + "\nInicializado en: " + sessionManager.getInitializationDate();
    }

    private User createUser(RegisterRequest request, UserRol rol) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo ya esta registrado"
            );
        }

        User user = User.builder()
                .name(request.getName().trim())
                .lastname(request.getLastname().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(cleanNullable(request.getPhone()))
                .rol(rol)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user, String token, String message) {
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .rol(user.getRol())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .message(message)
                .build();
    }

    private String cleanNullable(String value) {
        if (value == null) return null;
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }
}
