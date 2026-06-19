package com.fullstack.users.service;

import com.fullstack.users.dto.UserAddressRequest;
import com.fullstack.users.dto.UserAddressResponse;
import com.fullstack.users.model.User;
import com.fullstack.users.model.UserAddress;
import com.fullstack.users.repository.UserAddressRepository;
import com.fullstack.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    public List<UserAddressResponse> listMine(String email) {
        User user = getUserByEmail(email);
        return userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId())
                .stream()
                .map(UserAddressResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UserAddressResponse create(String email, UserAddressRequest request) {
        User user = getUserByEmail(email);
        boolean firstAddress = !userAddressRepository.existsByUserId(user.getId());
        boolean shouldBeDefault = firstAddress || request.isDefaultAddress();

        if (shouldBeDefault) {
            clearDefault(user.getId());
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .alias(cleanOrDefault(request.getAlias(), "Direccion"))
                .region(request.getRegion().trim())
                .comuna(request.getComuna().trim())
                .calle(request.getCalle().trim())
                .numero(request.getNumero().trim())
                .detalle(cleanNullable(request.getDetalle()))
                .defaultAddress(shouldBeDefault)
                .build();

        return UserAddressResponse.fromEntity(userAddressRepository.save(address));
    }

    @Transactional
    public UserAddressResponse update(String email, UUID addressId, UserAddressRequest request) {
        User user = getUserByEmail(email);
        UserAddress address = getOwnedAddress(user.getId(), addressId);

        if (request.isDefaultAddress()) {
            clearDefault(user.getId());
            address.setDefaultAddress(true);
        }

        address.setAlias(cleanOrDefault(request.getAlias(), "Direccion"));
        address.setRegion(request.getRegion().trim());
        address.setComuna(request.getComuna().trim());
        address.setCalle(request.getCalle().trim());
        address.setNumero(request.getNumero().trim());
        address.setDetalle(cleanNullable(request.getDetalle()));

        return UserAddressResponse.fromEntity(userAddressRepository.save(address));
    }

    @Transactional
    public void delete(String email, UUID addressId) {
        User user = getUserByEmail(email);
        UserAddress address = getOwnedAddress(user.getId(), addressId);
        boolean wasDefault = address.isDefaultAddress();

        userAddressRepository.delete(address);

        if (wasDefault) {
            userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setDefaultAddress(true);
                        userAddressRepository.save(next);
                    });
        }
    }

    @Transactional
    public UserAddressResponse setDefault(String email, UUID addressId) {
        User user = getUserByEmail(email);
        UserAddress address = getOwnedAddress(user.getId(), addressId);

        clearDefault(user.getId());
        address.setDefaultAddress(true);
        return UserAddressResponse.fromEntity(userAddressRepository.save(address));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
    }

    private UserAddress getOwnedAddress(UUID userId, UUID addressId) {
        return userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Direccion no encontrada"
                ));
    }

    private void clearDefault(UUID userId) {
        userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId)
                .forEach(address -> {
                    if (address.isDefaultAddress()) {
                        address.setDefaultAddress(false);
                        userAddressRepository.save(address);
                    }
                });
    }

    private String cleanOrDefault(String value, String fallback) {
        String clean = cleanNullable(value);
        return clean == null ? fallback : clean;
    }

    private String cleanNullable(String value) {
        if (value == null) return null;
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }
}
