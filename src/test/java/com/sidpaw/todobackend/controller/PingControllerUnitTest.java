package com.sidpaw.todobackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class PingControllerUnitTest {

    @InjectMocks
    private PingController pingController;

    private LocalDateTime fixedDateTime;

    @BeforeEach
    void setUp() {
        fixedDateTime = LocalDateTime.of(2025, 9, 23, 12, 30, 45);
    }

    @Test
    void givenFixedMockedTime_WhenCallingPing_ThenReturnsCorrectResponse() {
        // Given
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When
            ResponseEntity<Map<String, String>> response = pingController.ping();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("message")).isEqualTo("pong");
            assertThat(response.getBody().get("timestamp")).isEqualTo(fixedDateTime.toString());
        }
    }

    @Test
    void givenValidRequest_WhenCallingPing_ThenResponseContainsCorrectKeys() {
        // When
        ResponseEntity<Map<String, String>> response = pingController.ping();

        // Then
        assertThat(response.getBody()).containsKeys("message", "timestamp");
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void givenMultipleCalls_WhenCallingPing_ThenMessageIsAlwaysPong() {
        // When
        ResponseEntity<Map<String, String>> response1 = pingController.ping();
        ResponseEntity<Map<String, String>> response2 = pingController.ping();
        ResponseEntity<Map<String, String>> response3 = pingController.ping();

        // Then
        assertThat(response1.getBody().get("message")).isEqualTo("pong");
        assertThat(response2.getBody().get("message")).isEqualTo("pong");
        assertThat(response3.getBody().get("message")).isEqualTo("pong");
    }

    @Test
    void givenCurrentTime_WhenCallingPing_ThenTimestampIsCurrentTime() {
        // Given
        LocalDateTime beforeCall = LocalDateTime.now();

        // When
        ResponseEntity<Map<String, String>> response = pingController.ping();

        // Then
        LocalDateTime afterCall = LocalDateTime.now();
        String timestampStr = response.getBody().get("timestamp");
        LocalDateTime responseTimestamp = LocalDateTime.parse(timestampStr);

        assertThat(responseTimestamp).isBetween(beforeCall, afterCall);
    }

    @Test
    void givenValidRequest_WhenCallingPing_ThenReturnsOkStatus() {
        // When
        ResponseEntity<Map<String, String>> response = pingController.ping();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenMockedTime_WhenCallingPing_ThenReturnsExpectedTimestamp() {
        // Given
        LocalDateTime mockTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockTime);

            // When
            ResponseEntity<Map<String, String>> response = pingController.ping();

            // Then
            assertThat(response.getBody().get("timestamp")).isEqualTo("2025-01-01T00:00");
            assertThat(response.getBody().get("message")).isEqualTo("pong");
        }
    }

    @Test
    void givenSameMockedTime_WhenCallingPingMultipleTimes_ThenReturnsSameTimestamp() {
        // Given
        LocalDateTime mockTime = LocalDateTime.of(2025, 6, 15, 14, 30, 22);

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockTime);

            // When
            ResponseEntity<Map<String, String>> response1 = pingController.ping();
            ResponseEntity<Map<String, String>> response2 = pingController.ping();

            // Then
            assertThat(response1.getBody().get("timestamp")).isEqualTo(response2.getBody().get("timestamp"));
            assertThat(response1.getBody().get("timestamp")).isEqualTo("2025-06-15T14:30:22");
        }
    }

    @Test
    void givenValidRequest_WhenCallingPing_ThenResponseBodyIsNotNull() {
        // When
        ResponseEntity<Map<String, String>> response = pingController.ping();

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }
}
