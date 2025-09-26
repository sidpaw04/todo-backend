package com.sidpaw.todobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PingController.class)
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenValidRequest_WhenCallingPingEndpoint_ThenReturnsSuccessResponse() throws Exception {
        mockMvc.perform(get("/api/ping")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("pong"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.timestamp").value(not(emptyString())));
    }

    @Test
    void givenValidRequest_WhenCallingPingEndpoint_ThenReturnsCorrectStructure() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isMap())
                .andExpect(jsonPath("$", hasKey("message")))
                .andExpect(jsonPath("$", hasKey("timestamp")))
                .andExpect(jsonPath("$.message", is("pong")));
    }

    @Test
    void givenGetRequest_WhenCallingPingEndpoint_ThenAcceptsRequest() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void givenPostRequest_WhenCallingPingEndpoint_ThenRejectsWithMethodNotAllowed() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/ping"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void givenDeleteRequest_WhenCallingPingEndpoint_ThenRejectsWithMethodNotAllowed() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/ping"))
                .andExpect(status().isMethodNotAllowed());
    }
}