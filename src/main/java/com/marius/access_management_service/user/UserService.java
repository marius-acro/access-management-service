package com.marius.access_management_service.user;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Counter usersCreatedSuccess;
    private final Counter usersCreatedFailure;

    public UserService(UserRepository userRepository, MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.usersCreatedSuccess = Counter
                .builder("access-management-service.users.created")
                .tag("outcome", "success")
                .description("Users created through the API")
                .register(meterRegistry);
        this.usersCreatedFailure = Counter
                .builder("access-management-service.users.created")
                .tag("outcome", "failure")
                .description("Users created through the API")
                .register(meterRegistry);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User createUser(@NonNull CreateUserRequest request) {
        try {
            User user = userRepository.save(new User(request.email(), request.toRole()));
            this.usersCreatedSuccess.increment();

            return user;
        } catch (Exception e) {
            this.usersCreatedFailure.increment();
            throw e;
        }
    }

    public void deleteById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }
}
