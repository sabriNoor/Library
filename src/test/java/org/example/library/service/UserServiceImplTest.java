package org.example.library.service;

import org.example.library.dto.CreateUserRequest;
import org.example.library.entity.User;
import org.example.library.exception.ConflictException;
import org.example.library.exception.OperationNotAllowedException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.BorrowRepository;
import org.example.library.repository.UserRepository;
import org.example.library.service.impl.UserServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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

    // ✅ CREATE USER SUCCESS
    @Test
    void shouldCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest("Noor", "noor@gmail.com");

        User savedUser = User.builder()
                .id(1L)
                .name("Noor")
                .email("noor@gmail.com")
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var response = userService.createUser(request);

        assertEquals("Noor", response.name());
        assertEquals("noor@gmail.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    // ❌ CREATE USER - EMAIL EXISTS
    @Test
    void shouldThrowConflictWhenEmailExists() {
        CreateUserRequest request = new CreateUserRequest("Noor", "noor@gmail.com");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                userService.createUser(request)
        );

        verify(userRepository, never()).save(any());
    }

    // ✅ GET USER BY ID SUCCESS
    @Test
    void shouldReturnUserWhenFound() {
        User user = User.builder()
                .id(1L)
                .name("Noor")
                .email("noor@gmail.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userService.getUserById(1L);

        assertEquals("Noor", response.name());
    }

    // ❌ GET USER BY ID NOT FOUND
    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(1L)
        );
    }

    // ✅ GET ALL USERS
    @Test
    void shouldReturnAllUsers() {
        List<User> users = List.of(
                User.builder().id(1L).name("A").email("a@mail.com").build(),
                User.builder().id(2L).name("B").email("b@mail.com").build()
        );

        when(userRepository.findAll()).thenReturn(users);

        var result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    // ✅ DELETE USER SUCCESS
    @Test
    void shouldDeleteUserSuccessfully() {
        User user = User.builder().id(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(borrowRepository.existsByUserId(1L)).thenReturn(false);

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    // ❌ DELETE USER NOT FOUND
    @Test
    void shouldThrowWhenDeletingNonExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteUser(1L)
        );
    }

    // ❌ DELETE USER WITH ACTIVE BORROWS
    @Test
    void shouldThrowWhenUserHasActiveBorrows() {
        User user = User.builder().id(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(borrowRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(OperationNotAllowedException.class, () ->
                userService.deleteUser(1L)
        );

        verify(userRepository, never()).delete(any());
    }
}