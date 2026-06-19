package com.fullstack.users.repository;

import com.fullstack.users.model.User;
import com.fullstack.users.model.UserRol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRol(UserRol rol);
}