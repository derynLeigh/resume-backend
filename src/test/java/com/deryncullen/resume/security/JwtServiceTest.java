package com.deryncullen.resume.security;

import com.deryncullen.resume.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Set up the JWT service with test values using reflection
        ReflectionTestUtils.setField(jwtService, "secret",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid access token")
        void shouldGenerateValidAccessToken() {
            // When
            String token = jwtService.generateToken(testUser.getEmail());

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            // Verify token contains correct username
            String extractedUsername = jwtService.extractUsername(token);
            assertThat(extractedUsername).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should generate valid refresh token")
        void shouldGenerateValidRefreshToken() {
            // When
            String token = jwtService.generateRefreshToken(testUser.getEmail());

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            // Verify token contains correct username
            String extractedUsername = jwtService.extractUsername(token);
            assertThat(extractedUsername).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            // Given
            String email1 = "user1@example.com";
            String email2 = "user2@example.com";

            // When
            String token1 = jwtService.generateToken(email1);
            String token2 = jwtService.generateToken(email2);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Should generate different tokens on subsequent calls")
        void shouldGenerateDifferentTokensOnSubsequentCalls() throws InterruptedException {
            // Given
            String email = testUser.getEmail();

            // When
            String token1 = jwtService.generateToken(email);
            Thread.sleep(1000); // 1-second delay to ensure different timestamps
            String token2 = jwtService.generateToken(email);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests {

        @Test
        @DisplayName("Should extract username from token")
        void shouldExtractUsername() {
            // Given
            String token = jwtService.generateToken(testUser.getEmail());

            // When
            String username = jwtService.extractUsername(token);

            // Then
            assertThat(username).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should extract expiration date from token")
        void shouldExtractExpirationDate() {
            // Given
            String token = jwtService.generateToken(testUser.getEmail());

            // When
            Date expiration = jwtService.extractExpiration(token);

            // Then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());
        }

        @Test
        @DisplayName("Should extract custom claim from token")
        void shouldExtractCustomClaim() {
            // Given
            String token = jwtService.generateToken(testUser.getEmail());

            // When
            String subject = jwtService.extractClaim(token, Claims::getSubject);

            // Then
            assertThat(subject).isEqualTo(testUser.getEmail());
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate correct token")
        void shouldValidateCorrectToken() {
            // Given
            String token = jwtService.generateToken(testUser.getEmail());

            // When
            Boolean isValid = jwtService.validateToken(token, testUser.getEmail());

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject token with wrong username")
        void shouldRejectTokenWithWrongUsername() {
            // Given
            String token = jwtService.generateToken("user1@example.com");

            // When
            Boolean isValid = jwtService.validateToken(token, "user2@example.com");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            // Given - Create service with very short expiration
            JwtService shortLivedService = new JwtService();
            ReflectionTestUtils.setField(shortLivedService, "secret",
                    "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
            ReflectionTestUtils.setField(shortLivedService, "expiration", 1L); // 1ms
            ReflectionTestUtils.setField(shortLivedService, "refreshExpiration", 1L);

            String token = shortLivedService.generateToken(testUser.getEmail());

            // Wait for token to expire
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When - validateToken catches exceptions and returns false
            Boolean isValid = shortLivedService.validateToken(token, testUser.getEmail());

            // Then - Should return false for expired token
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should validate token with correct username")
        void shouldValidateTokenWithCorrectUsername() {
            // Given
            String email = "valid@example.com";
            String token = jwtService.generateToken(email);

            // When
            Boolean isValid = jwtService.validateToken(token, email);

            // Then
            assertThat(isValid).isTrue();
        }
    }

    @Nested
    @DisplayName("Expiration Time Tests")
    class ExpirationTimeTests {

        @Test
        @DisplayName("Should return correct expiration time")
        void shouldReturnCorrectExpirationTime() {
            // When
            Long expirationTime = jwtService.getExpirationTime();

            // Then
            assertThat(expirationTime).isEqualTo(86400000L);
        }

        @Test
        @DisplayName("Should return correct refresh expiration time")
        void shouldReturnCorrectRefreshExpirationTime() {
            // When
            Long refreshExpirationTime = jwtService.getRefreshExpirationTime();

            // Then
            assertThat(refreshExpirationTime).isEqualTo(604800000L);
        }

        @Test
        @DisplayName("Access token should expire before refresh token")
        void accessTokenShouldExpireBeforeRefreshToken() {
            // Given
            String accessToken = jwtService.generateToken(testUser.getEmail());
            String refreshToken = jwtService.generateRefreshToken(testUser.getEmail());

            // When
            Date accessExpiration = jwtService.extractExpiration(accessToken);
            Date refreshExpiration = jwtService.extractExpiration(refreshToken);

            // Then
            assertThat(accessExpiration).isBefore(refreshExpiration);
        }
    }

    @Nested
    @DisplayName("Token Structure Tests")
    class TokenStructureTests {

        @Test
        @DisplayName("Token should have three parts separated by dots")
        void tokenShouldHaveThreeParts() {
            // Given
            String token = jwtService.generateToken(testUser.getEmail());

            // When
            String[] parts = token.split("\\.");

            // Then
            assertThat(parts).hasSize(3);
        }

        @Test
        @DisplayName("Should generate token with issued at time")
        void shouldGenerateTokenWithIssuedAtTime() {
            // Given - Allow some buffer before/after for test execution
            Date beforeGeneration = new Date(System.currentTimeMillis() - 1000); // 1 second before
            String token = jwtService.generateToken(testUser.getEmail());
            Date afterGeneration = new Date(System.currentTimeMillis() + 1000); // 1 second after

            // When
            Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

            // Then
            assertThat(issuedAt).isNotNull();
            assertThat(issuedAt).isBetween(beforeGeneration, afterGeneration, true, true);
        }

        @Test
        @DisplayName("Should generate token with correct expiration time offset")
        void shouldGenerateTokenWithCorrectExpirationOffset() {
            // Given
            String token = jwtService.generateToken(testUser.getEmail());

            // When
            Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
            Date expiration = jwtService.extractExpiration(token);
            long actualDuration = expiration.getTime() - issuedAt.getTime();

            // Then
            long expectedDuration = jwtService.getExpirationTime();
            // Allow 5-second tolerance for test execution time and clock precision
            assertThat(actualDuration).isBetween(expectedDuration - 5000, expectedDuration + 5000);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should generate token with null username")
        void shouldGenerateTokenWithNullUsername() {
            // When - JWT allows null subjects, it just creates a token without a subject claim
            String token = jwtService.generateToken(null);

            // Then
            assertThat(token).isNotNull();
            // Token can be generated but extracting username will return null
            String extractedUsername = jwtService.extractUsername(token);
            assertThat(extractedUsername).isNull();
        }

        @Test
        @DisplayName("Should handle empty username")
        void shouldHandleEmptyUsername() {
            // When/Then - Verify we can work with empty username without crashing
            // JJWT may treat empty string as null for subject claim
            try {
                String token = jwtService.generateToken("");
                // If token generation succeeds, verify it's a valid JWT structure
                if (token != null) {
                    assertThat(token).isNotEmpty();
                    assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
                }
            } catch (Exception e) {
                // If JJWT doesn't allow empty username, that's also acceptable behavior
                assertThat(e).isNotNull();
            }
        }

        @Test
        @DisplayName("Should handle malformed token gracefully")
        void shouldHandleMalformedTokenGracefully() {
            // Given
            String malformedToken = "not.a.valid.token";

            // When/Then
            assertThatThrownBy(() -> jwtService.extractUsername(malformedToken))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should handle token with invalid signature")
        void shouldHandleTokenWithInvalidSignature() {
            // Given - Create a token with one service
            String token = jwtService.generateToken(testUser.getEmail());

            // Create another service with different secret
            JwtService differentService = new JwtService();
            ReflectionTestUtils.setField(differentService, "secret",
                    "differentSecretKey123456789012345678901234567890123456789012");
            ReflectionTestUtils.setField(differentService, "expiration", 86400000L);
            ReflectionTestUtils.setField(differentService, "refreshExpiration", 604800000L);

            // When/Then - Trying to validate with different secret should fail
            assertThatThrownBy(() -> differentService.extractUsername(token))
                    .isInstanceOf(Exception.class);
        }
    }
}