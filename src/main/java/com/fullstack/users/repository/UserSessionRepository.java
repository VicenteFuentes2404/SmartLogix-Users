package com.fullstack.users.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fullstack.users.model.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByToken(String token);

    @Query("""
           SELECT s
           FROM UserSession s
           JOIN FETCH s.user
           WHERE s.active = true
           AND s.expiresAt > :now
           """)
    List<UserSession> findActiveSessions(@Param("now") LocalDateTime now);
}

//Este repositorio permite que el sessionmanager cargue las sesiones activas al inicar el servicio