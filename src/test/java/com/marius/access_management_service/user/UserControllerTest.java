package com.marius.access_management_service.user;

import com.marius.access_management_service.user.role.Role;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // Get Users
    @Test
    void givenNoUsers_whenGetUsers_thenReturns200() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void givenUsers_whenGetUsers_thenReturnsUsers() throws Exception {
        User user1 = userRepository.save(new User("test1@example.com", Role.ADMIN));
        User user2 = userRepository.save(new User("test2@example.com", Role.GUEST));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.email == 'test1@example.com')]").exists())
                .andExpect(jsonPath("$[?(@.email == 'test2@example.com')]").exists())
                .andExpect(jsonPath("$[?(@.role == 'ADMIN')]").exists())
                .andExpect(jsonPath("$[?(@.role == 'GUEST')]").exists());
    }

    // Get User By Id
    @Test
    void givenUser_whenGetUserById_thenReturns200() throws Exception {
        User user = userRepository.save(new User("test@example.com", Role.ADMIN));

        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void whenGetUserByNonexistentId_thenReturns404() throws Exception {
        mockMvc.perform(get("/users/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetUserByInvalidId_thenReturns400() throws Exception {
        mockMvc.perform(get("/users/" + "randomstring"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    // Create User
    @Test
    void givenValidRequest_whenCreateUser_thenReturns201() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void givenRequestWithEmptyBody_whenCreateUser_thenReturns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.role").exists());
        ;
    }

    @Test
    void givenRequestWithEmptyEmail_whenCreateUser_thenReturns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "",
                                    "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.role").doesNotExist());
    }

    @Test
    void givenRequestWithInvalidEmail_whenCreateUser_thenReturns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "invalidemail",
                                    "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.role").doesNotExist());
    }

    @Test
    void givenRequestWithEmptyRole_whenCreateUser_thenReturns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "role": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").doesNotExist())
                .andExpect(jsonPath("$.fields.role").exists());
    }

    @Test
    void givenRequestWithInvalidRole_whenCreateUser_thenReturns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "role": "invalidrole"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").doesNotExist())
                .andExpect(jsonPath("$.fields.role").exists());
    }

    // Delete User
    @Test
    void givenUser_whenDeleteUserById_thenReturns204() throws Exception {
        User user = userRepository.save(new User("test@example.com", Role.ADMIN));

        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(user.getId()).isEmpty());
    }

    @Test
    void whenDeleteUserByNonexistentId_thenReturns404() throws Exception {
        mockMvc.perform(delete("/users/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void whenDeleteUserByInvalidId_thenReturns400() throws Exception {
        mockMvc.perform(delete("/users/" + "randomstring"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}
