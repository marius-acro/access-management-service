package com.marius.access_management_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class CorrelationIdFilterTest {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenNoCorrelationIdHeader_whenRequestIsMade_thenCorrelationIdIsGenerated() throws Exception {
        String correlationId = mockMvc.perform(get("/users"))
                .andReturn()
                .getResponse()
                .getHeader(CORRELATION_ID_HEADER);

        assertNotNull(correlationId);
        assertDoesNotThrow(() -> UUID.fromString(correlationId));
    }

    @Test
    void givenIncomingRequestHasCorrelationIdHeader_whenRequestIsMade_thenIncomingCorrelationIdIsPropagated() throws Exception {
        String givenCorrelationId = UUID.randomUUID().toString();

        String returnedCorrelationId = mockMvc.perform(
                    get("/users").header(CORRELATION_ID_HEADER, givenCorrelationId)
                )
                .andReturn()
                .getResponse()
                .getHeader(CORRELATION_ID_HEADER);

        assertEquals(givenCorrelationId, returnedCorrelationId);
    }

    @Test
    void givenNoCorrelationIdHeader_whenMultipleRequestsAreMade_thenEachGetsUniqueId() throws Exception {
        String returnedCorrelationId1 = mockMvc.perform(get("/users"))
                .andReturn()
                .getResponse()
                .getHeader(CORRELATION_ID_HEADER);

        String returnedCorrelationId2 = mockMvc.perform(get("/users"))
                .andReturn()
                .getResponse()
                .getHeader(CORRELATION_ID_HEADER);

        assertNotEquals(returnedCorrelationId1, returnedCorrelationId2);
    }
}