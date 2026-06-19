package com.fullstack.users.singleton;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.fullstack.users.model.User;
import com.fullstack.users.model.UserSession;
import com.fullstack.users.repository.UserSessionRepository;
import com.fullstack.users.security.JwtService;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

//esta clase es un singleton que se encarga de gestionar las sesiones de los usuarios,
// manteniendo una lista en memoria de las sesiones activas para permitir una validación rápida de los tokens JWT,
// y sincronizando esta información con la base de datos para garantizar la persistencia y el control.

//@component hace que spring cree una sola instancia de esta clase y la comparta en toda la aplicación.
@Component
@RequiredArgsConstructor
public class SessionManager {

    private final UserSessionRepository userSessionRepository;
    private final JwtService jwtService;

    private final Map<String, UUID> activeSessions = new ConcurrentHashMap<>();

    private LocalDateTime initializationDate;

    @PostConstruct
    public void initialize() {
        this.initializationDate = LocalDateTime.now();

        userSessionRepository.findActiveSessions(LocalDateTime.now())
                .forEach(session -> activeSessions.put(
                        session.getToken(),
                        session.getUser().getId()
                ));

        System.out.println("[Singleton SessionManager] Sesiones activas cargadas: "
                + activeSessions.size());
    }


    //se crea un jwt real, se guarda la sesión en la base de datos, y se agrega a la lista de sesiones activas en memoria.
    //y se devuelve el token al login
    @Transactional
    public String createSession(User user) {
        String token = jwtService.generateToken(user);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(Duration.ofMillis(jwtService.getExpirationMs()));

        UserSession session = UserSession.builder()
                .token(token)
                .user(user)
                .active(true)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        userSessionRepository.save(session);
        activeSessions.put(token, user.getId());

        return token;
    }

    public boolean isSessionActive(String token) {
        return activeSessions.containsKey(token);
    }

    @Transactional
    public void closeSession(String token) {
        activeSessions.remove(token);

        userSessionRepository.findByToken(token)
                .ifPresent(session -> {
                    session.setActive(false);
                    session.setClosedAt(LocalDateTime.now());
                    userSessionRepository.save(session);
                });
    }

    public int getActiveSessionsCount() {
        return activeSessions.size();
    }

    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    public int getInstanceHashCode() {
        return System.identityHashCode(this);
    }
}