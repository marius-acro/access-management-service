package com.marius.access_management_service.user;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public User createUser(@NonNull CreateUserRequest request) {
        return userRepository.save(new User(request.email(), request.role()));
    }

    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }
}
