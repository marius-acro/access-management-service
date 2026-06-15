package com.marius.access_management_service;

import com.marius.access_management_service.user.role.Role;
import com.marius.access_management_service.user.User;
import com.marius.access_management_service.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        userRepository.save(new User("email1@test.com", Role.ADMIN));
        userRepository.save(new User("email2@test.com", Role.MEMBER));
        userRepository.save(new User("email3@test.com", Role.GUEST));
    }
}
