package com.fullstack.users.repository;

import com.fullstack.users.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUserIdOrderByDefaultAddressDescCreatedAtAsc(UUID userId);

    Optional<UserAddress> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserId(UUID userId);
}
