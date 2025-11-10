package com.deryncullen.resume.security;

import com.deryncullen.resume.dto.AuthenticationRequest;
import com.deryncullen.resume.dto.AuthenticationResponse;
import com.deryncullen.resume.dto.RegisterRequest;
import com.deryncullen.resume.model.User;
import com.deryncullen.resume.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        authRequest = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUser() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(anyString())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);

            // When
            AuthenticationResponse response = authenticationService.register(registerRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(86400000L);

            verify(userRepository).existsByEmail("test@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
            verify(jwtService).generateToken("test@example.com");
            verify(jwtService).generateRefreshToken("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> authenticationService.register(registerRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            verify(userRepository).existsByEmail("test@example.com");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate user successfully")
        void shouldAuthenticateUser() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(anyString())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);

            // When
            AuthenticationResponse response = authenticationService.authenticate(authRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(86400000L);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail("test@example.com");
            verify(jwtService).generateToken("test@example.com");
            verify(jwtService).generateRefreshToken("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authenticationService.authenticate(authRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail("test@example.com");
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshToken() {
            // Given
            String refreshToken = "valid-refresh-token";
            when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(jwtService.validateToken(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateToken(anyString())).thenReturn("new-access-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);

            // When
            AuthenticationResponse response = authenticationService.refreshToken(refreshToken);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(86400000L);

            verify(jwtService).extractUsername(refreshToken);
            verify(userRepository).findByEmail("test@example.com");
            verify(jwtService).validateToken(refreshToken, "test@example.com");
            verify(jwtService).generateToken("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception when refresh token is invalid")
        void shouldThrowExceptionWhenRefreshTokenInvalid() {
            // Given
            String refreshToken = "invalid-refresh-token";
            when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(jwtService.validateToken(anyString(), anyString())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authenticationService.refreshToken(refreshToken))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid refresh token");

            verify(jwtService).extractUsername(refreshToken);
            verify(userRepository).findByEmail("test@example.com");
            verify(jwtService).validateToken(refreshToken, "test@example.com");
            verify(jwtService, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Should throw exception when user not found for refresh token")
        void shouldThrowExceptionWhenUserNotFoundForRefreshToken() {
            // Given
            String refreshToken = "valid-refresh-token";
            when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authenticationService.refreshToken(refreshToken))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");

            verify(jwtService).extractUsername(refreshToken);
            verify(userRepository).findByEmail("test@example.com");
            verify(jwtService, never()).validateToken(anyString(), anyString());
        }
    }
}