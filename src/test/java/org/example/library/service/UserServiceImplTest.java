package org.example.library.service;

import org.example.library.dto.CreateUserRequest;
import org.example.library.entity.User;
import org.example.library.exception.ConflictException;
import org.example.library.exception.OperationNotAllowedException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.BorrowRepository;
import org.example.library.repository.UserRepository;
import org.example.library.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Noor")
                .email("noor@gmail.com")
                .build();
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest("Noor", "noor@gmail.com");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        var response = userService.createUser(request);

        assertThat(response.name()).isEqualTo("Noor");
        assertThat(response.email()).isEqualTo("noor@gmail.com");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw conflict when email already exists")
    void shouldThrowConflictWhenEmailExists() {
        CreateUserRequest request = new CreateUserRequest("Noor", "noor@gmail.com");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return user by id")
    void shouldReturnUserWhenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userService.getUserById(1L);

        assertThat(response.name()).isEqualTo("Noor");

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        var result = userService.getAllUsers();

        assertThat(result).hasSize(1);

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should delete user successfully when no active borrows")
    void shouldDeleteUserSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(borrowRepository.existsByUserId(1L)).thenReturn(false);

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Should throw when deleting non-existing user")
    void shouldThrowWhenDeletingNonExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when user has active borrows")
    void shouldThrowWhenUserHasActiveBorrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(borrowRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(OperationNotAllowedException.class);

        verify(userRepository, never()).delete(any());
    }
}