package org.example.library.service.impl;

import org.example.library.dto.CreateUserRequest;
import org.example.library.dto.UserResponse;
import org.example.library.entity.User;
import org.example.library.exception.ConflictException;
import org.example.library.exception.OperationNotAllowedException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.BorrowRepository;
import org.example.library.repository.UserRepository;
import org.example.library.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;

    public UserServiceImpl(UserRepository userRepository,
                           BorrowRepository borrowRepository) {
        this.userRepository = userRepository;
        this.borrowRepository = borrowRepository;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use");
        }

        User newUser = User.builder()
                .email(request.email())
                .name(request.name())
                .build();

        return mapToResponse(userRepository.save(newUser));
    }

    @Override
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));

        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));

        if (borrowRepository.existsByUserId(id)) {
            throw new OperationNotAllowedException(
                    "Cannot delete user with active borrows");
        }

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}