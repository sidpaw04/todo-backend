package com.sidpaw.todobackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PingControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void givenRunningApplication_WhenCallingPingEndpoint_ThenReturnsSuccessResponse() {
        // Given
        String url = "http://localhost:" + port + "/api/ping";

        // When
        ResponseEntity<PingResponse> response = restTemplate.getForEntity(url, PingResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("pong");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isInstanceOf(String.class);
    }

    @Test
    void givenRunningApplication_WhenCallingPingEndpoint_ThenReturnsCorrectHeaders() {
        // Given
        String url = "http://localhost:" + port + "/api/ping";

        // When
        ResponseEntity<PingResponse> response = restTemplate.getForEntity(url, PingResponse.class);

        // Then
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");
    }

    @Test
    void givenRunningApplication_WhenCallingPingEndpointMultipleTimes_ThenAllRequestsSucceed() {
        // Given
        String url = "http://localhost:" + port + "/api/ping";

        // When - Multiple requests
        ResponseEntity<PingResponse> response1 = restTemplate.getForEntity(url, PingResponse.class);
        ResponseEntity<PingResponse> response2 = restTemplate.getForEntity(url, PingResponse.class);
        ResponseEntity<PingResponse> response3 = restTemplate.getForEntity(url, PingResponse.class);

        // Then - All should be successful
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And timestamps should be different (assuming some time difference)
        String timestamp1 = response1.getBody().getTimestamp();
        String timestamp2 = response2.getBody().getTimestamp();
        String timestamp3 = response3.getBody().getTimestamp();

        // At least one timestamp should be different (due to time progression)
        boolean timestampsDiffer = !timestamp1.equals(timestamp2) || 
                                   !timestamp2.equals(timestamp3) || 
                                   !timestamp1.equals(timestamp3);
        assertThat(timestampsDiffer).isTrue();
    }

    @Test
    void givenRunningApplication_WhenCallingActuatorHealthEndpoint_ThenReturnsHealthStatus() {
        // Given
        String url = "http://localhost:" + port + "/actuator/health";

        // When
        ResponseEntity<HealthResponse> response = restTemplate.getForEntity(url, HealthResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("UP");
    }

    /**
     * Inner static class for type-safe ping response handling.
     */
    static class PingResponse {
        private String message;
        private String timestamp;

        // Default constructor for Jackson
        public PingResponse() {}

        public PingResponse(String message, String timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * Inner static class for type-safe health response handling.
     */
    static class HealthResponse {
        private String status;

        // Default constructor for Jackson
        public HealthResponse() {}

        public HealthResponse(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
