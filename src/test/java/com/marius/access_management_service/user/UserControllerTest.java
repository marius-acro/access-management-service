package com.marius.access_management_service.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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

    @Test
    void givenUser_whenGetUserById_thenReturns200() throws Exception {
        User user = userRepository.save(new User("test@example.com", Role.ADMIN));

        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
    
    // nicht-existierende UUID → 404 (noch nicht implementiert, braucht Fehlerhandling)
    // invalider String statt UUID → 400

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
    void givenUser_whenDeleteUserById_thenReturns204() throws Exception {
        User user = userRepository.save(new User("test@example.com", Role.ADMIN));

        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(user.getId()).isEmpty());
    }
}
