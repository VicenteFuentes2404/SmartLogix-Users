package com.fullstack.users.service;

import com.fullstack.users.dto.UserAddressRequest;
import com.fullstack.users.dto.UserAddressResponse;
import com.fullstack.users.model.User;
import com.fullstack.users.model.UserAddress;
import com.fullstack.users.model.UserRol;
import com.fullstack.users.model.UserStatus;
import com.fullstack.users.repository.UserAddressRepository;
import com.fullstack.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @InjectMocks
    private UserAddressService userAddressService;

    @Test
    void create_whenFirstAddress_savesDefaultAddressResponse() {
        // Arrange
        User user = user();
        UserAddressRequest request = addressRequest(false);
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(userAddressRepository.existsByUserId(user.getId())).thenReturn(false);
        when(userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId())).thenReturn(List.of());
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> {
            UserAddress address = invocation.getArgument(0);
            address.setId(UUID.randomUUID());
            address.setCreatedAt(LocalDateTime.now());
            return address;
        });

        // Act
        UserAddressResponse response = userAddressService.create("cliente@smartlogix.cl", request);

        // Assert
        assertEquals("Casa", response.getAlias());
        assertEquals("Metropolitana de Santiago", response.getRegion());
        assertTrue(response.isDefaultAddress());
        verify(userAddressRepository).save(any(UserAddress.class));
    }

    @Test
    void listMine_returnsAddressResponseDtos() {
        // Arrange
        User user = user();
        UserAddress address = address(user);
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId()))
                .thenReturn(List.of(address));

        // Act
        List<UserAddressResponse> responses = userAddressService.listMine("cliente@smartlogix.cl");

        // Assert
        assertEquals(1, responses.size());
        assertEquals(address.getId(), responses.get(0).getId());
        assertEquals("Providencia", responses.get(0).getComuna());
    }

    @Test
    void update_whenRequestIsDefault_clearsPreviousDefaultAndSavesAddress() {
        // Arrange
        User user = user();
        UserAddress previousDefault = address(user);
        previousDefault.setDefaultAddress(true);
        UserAddress target = address(user);
        target.setId(UUID.randomUUID());
        target.setDefaultAddress(false);
        UserAddressRequest request = addressRequest(true);
        request.setAlias("Oficina");
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(target.getId(), user.getId())).thenReturn(Optional.of(target));
        when(userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId()))
                .thenReturn(List.of(previousDefault, target));
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserAddressResponse response = userAddressService.update("cliente@smartlogix.cl", target.getId(), request);

        // Assert
        assertEquals("Oficina", response.getAlias());
        assertTrue(response.isDefaultAddress());
        assertFalse(previousDefault.isDefaultAddress());
        verify(userAddressRepository).save(target);
    }

    @Test
    void delete_whenDefaultAddress_removesItAndPromotesNextAddress() {
        // Arrange
        User user = user();
        UserAddress deleted = address(user);
        deleted.setDefaultAddress(true);
        UserAddress next = address(user);
        next.setId(UUID.randomUUID());
        next.setDefaultAddress(false);
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(deleted.getId(), user.getId())).thenReturn(Optional.of(deleted));
        when(userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId()))
                .thenReturn(List.of(next));

        // Act
        userAddressService.delete("cliente@smartlogix.cl", deleted.getId());

        // Assert
        assertTrue(next.isDefaultAddress());
        verify(userAddressRepository).delete(deleted);
        verify(userAddressRepository).save(next);
    }

    @Test
    void setDefault_whenAddressExists_marksOnlyTargetAsDefault() {
        // Arrange
        User user = user();
        UserAddress previousDefault = address(user);
        previousDefault.setDefaultAddress(true);
        UserAddress target = address(user);
        target.setId(UUID.randomUUID());
        target.setDefaultAddress(false);
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(target.getId(), user.getId())).thenReturn(Optional.of(target));
        when(userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId()))
                .thenReturn(List.of(previousDefault, target));
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserAddressResponse response = userAddressService.setDefault("cliente@smartlogix.cl", target.getId());

        // Assert
        assertTrue(response.isDefaultAddress());
        assertFalse(previousDefault.isDefaultAddress());
        verify(userAddressRepository).save(target);
    }

    @Test
    void update_whenAddressDoesNotBelongToUser_throwsNotFound() {
        // Arrange
        User user = user();
        UUID addressId = UUID.randomUUID();
        when(userRepository.findByEmail("cliente@smartlogix.cl")).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(addressId, user.getId())).thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userAddressService.update("cliente@smartlogix.cl", addressId, addressRequest(false))
        );

        // Assert
        assertEquals(404, exception.getStatusCode().value());
    }

    private UserAddressRequest addressRequest(boolean defaultAddress) {
        UserAddressRequest request = new UserAddressRequest();
        request.setAlias(" Casa ");
        request.setRegion(" Metropolitana de Santiago ");
        request.setComuna(" Providencia ");
        request.setCalle(" Los Leones ");
        request.setNumero("123");
        request.setDetalle("Depto 404");
        request.setDefaultAddress(defaultAddress);
        return request;
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Cliente")
                .lastname("SmartLogix")
                .email("cliente@smartlogix.cl")
                .password("hash")
                .rol(UserRol.CLIENT)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private UserAddress address(User user) {
        return UserAddress.builder()
                .id(UUID.randomUUID())
                .user(user)
                .alias("Casa")
                .region("Metropolitana de Santiago")
                .comuna("Providencia")
                .calle("Los Leones")
                .numero("123")
                .detalle("Depto 404")
                .defaultAddress(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
