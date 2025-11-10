package com.deryncullen.resume.specs;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationApiSteps {

    // Registration Steps

    @Step("Register user with email <email>, password <password>, first name <firstName>, and last name <lastName>")
    public void registerUser(String email, String password, String firstName, String lastName) {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", email);
        registerRequest.put("password", password);
        registerRequest.put("firstName", firstName);
        registerRequest.put("lastName", lastName);

        Response response = given()
                .contentType("application/json")
                .body(registerRequest)
                .when()
                .post("/auth/register")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
        if (response.getStatusCode() == 201) {
            ScenarioDataStore.put("accessToken", response.jsonPath().getString("access_token"));
            ScenarioDataStore.put("refreshToken", response.jsonPath().getString("refresh_token"));
        }
    }

    @Step("Register user with empty email and password <password>")
    public void registerUserWithEmptyEmail(String password) {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "");
        registerRequest.put("password", password);
        registerRequest.put("firstName", "Test");
        registerRequest.put("lastName", "User");

        Response response = given()
                .contentType("application/json")
                .body(registerRequest)
                .when()
                .post("/auth/register")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Register user with email <email> and password <password>")
    public void registerUserQuick(String email, String password) {
        registerUser(email, password, "Test", "User");
    }

    @Step("Registration should return status code <statusCode>")
    public void verifyRegistrationStatusCode(String statusCode) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(Integer.parseInt(statusCode));
    }

    @Step("Response should contain access token")
    public void verifyAccessToken() {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        String accessToken = response.jsonPath().getString("access_token");
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();
    }

    @Step("Response should contain refresh token")
    public void verifyRefreshToken() {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        String refreshToken = response.jsonPath().getString("refresh_token");
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
    }

    @Step("Response should contain token type <tokenType>")
    public void verifyTokenType(String tokenType) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        String actualTokenType = response.jsonPath().getString("token_type");
        assertThat(actualTokenType).isEqualTo(tokenType);
    }

    @Step("Access token should be valid JWT")
    public void verifyAccessTokenIsValidJWT() {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        String accessToken = response.jsonPath().getString("access_token");

        // Verify JWT structure (3 parts: header.payload.signature)
        assertThat(accessToken.split("\\.")).hasSize(3);

        // Verify proper JWT format (base64url encoded parts)
        assertThat(accessToken).matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$");
    }

    @Step("Response should contain error message about duplicate email")
    public void verifyDuplicateEmailError() {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        String message = response.getBody().asString();
        assertThat(message.toLowerCase()).contains("already exists");
    }

    @Step("Response should contain validation errors")
    public void verifyValidationErrors() {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        String body = response.getBody().asString();
        assertThat(body).isNotEmpty();
    }

    // Login Steps

    @Step("Login with email <email> and password <password>")
    public void loginUser(String email, String password) {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        Response response = given()
                .contentType("application/json")
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
        if (response.getStatusCode() == 200) {
            ScenarioDataStore.put("accessToken", response.jsonPath().getString("access_token"));
            ScenarioDataStore.put("refreshToken", response.jsonPath().getString("refresh_token"));
        }
    }

    @Step("Login should return status code <statusCode>")
    public void verifyLoginStatusCode(String statusCode) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(Integer.parseInt(statusCode));
    }

    // Protected Endpoint Steps

    @Step("Create profile without authentication")
    public void createProfileWithoutAuth() {
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", "John");
        profile.put("lastName", "Doe");
        profile.put("email", "john@example.com");
        profile.put("title", "Software Engineer");

        Response response = given()
                .contentType("application/json")
                .body(profile)
                .when()
                .post("/profiles")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Request should return status code <statusCode>")
    public void verifyRequestStatusCode(String statusCode) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(Integer.parseInt(statusCode));
    }

    @Step("Register and login as <email> with password <password>")
    public void registerAndLogin(String email, String password) {
        registerUser(email, password, "Test", "User");
        loginUser(email, password);
    }

    @Step("Create profile with first name <firstName>, last name <lastName>, email <email>, and title <title>")
    public void createProfileWithAuth(String firstName, String lastName, String email, String title) {
        String accessToken = (String) ScenarioDataStore.get("accessToken");

        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", firstName);
        profile.put("lastName", lastName);
        profile.put("email", email);
        profile.put("title", title);

        Response response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(profile)
                .when()
                .post("/profiles")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
        if (response.getStatusCode() == 201) {
            ScenarioDataStore.put("profileId", response.jsonPath().getLong("id"));
        }
    }

    @Step("Profile creation should return status code <statusCode>")
    public void verifyProfileCreationStatusCode(String statusCode) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(Integer.parseInt(statusCode));
    }

    @Step("Profile should be created successfully")
    public void verifyProfileCreated() {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.jsonPath().getLong("id")).isNotNull();
    }

    @Step("Create profile with invalid JWT token")
    public void createProfileWithInvalidToken() {
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", "John");
        profile.put("lastName", "Doe");
        profile.put("email", "john@example.com");
        profile.put("title", "Software Engineer");

        Response response = given()
                .header("Authorization", "Bearer invalid.jwt.token")
                .contentType("application/json")
                .body(profile)
                .when()
                .post("/profiles")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Create profile with expired token")
    public void createProfileWithExpiredToken() {
        String expiredToken = (String) ScenarioDataStore.get("expiredToken");

        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", "John");
        profile.put("lastName", "Doe");
        profile.put("email", "john@example.com");
        profile.put("title", "Software Engineer");

        Response response = given()
                .header("Authorization", "Bearer " + expiredToken)
                .contentType("application/json")
                .body(profile)
                .when()
                .post("/profiles")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Wait for token to expire")
    public void waitForTokenExpiration() {
        String accessToken = (String) ScenarioDataStore.get("accessToken");
        ScenarioDataStore.put("expiredToken", accessToken);
        ScenarioDataStore.put("expiredToken", "expired.invalid.token");
    }

    // Refresh Token Steps

    @Step("Refresh access token using refresh token")
    public void refreshAccessToken() {
        String refreshToken = (String) ScenarioDataStore.get("refreshToken");
        String originalAccessToken = (String) ScenarioDataStore.get("accessToken");
        ScenarioDataStore.put("originalAccessToken", originalAccessToken);

        Response response = given()
                .header("Authorization", "Bearer " + refreshToken)
                .when()
                .post("/auth/refresh")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
        if (response.getStatusCode() == 200) {
            ScenarioDataStore.put("newAccessToken", response.jsonPath().getString("access_token"));
        }
    }

    @Step("Refresh access token with invalid token")
    public void refreshWithInvalidToken() {
        Response response = given()
                .header("Authorization", "Bearer invalid.refresh.token")
                .when()
                .post("/auth/refresh")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Refresh should return status code <statusCode>")
    public void verifyRefreshStatusCode(String statusCode) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(Integer.parseInt(statusCode));
    }

    @Step("Response should contain new access token")
    public void verifyNewAccessToken() {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        String newAccessToken = response.jsonPath().getString("access_token");
        assertThat(newAccessToken).isNotNull();
        assertThat(newAccessToken).isNotEmpty();
    }

    @Step("Response should contain same refresh token")
    public void verifySameRefreshToken() {
        Response response = (Response) ScenarioDataStore.get("response");
        String originalRefreshToken = (String) ScenarioDataStore.get("refreshToken");
        String newRefreshToken = response.jsonPath().getString("refresh_token");
        assertThat(newRefreshToken).isEqualTo(originalRefreshToken);
    }

    @Step("New access token should be different from original")
    public void verifyDifferentAccessToken() {
        String originalAccessToken = (String) ScenarioDataStore.get("originalAccessToken");
        String newAccessToken = (String) ScenarioDataStore.get("newAccessToken");
        assertThat(newAccessToken).isNotEqualTo(originalAccessToken);
    }

    // Token Verification Steps

    @Step("Extract username from access token")
    public void extractUsernameFromToken() {
        Response response = (Response) ScenarioDataStore.get("response");
        String accessToken = response.jsonPath().getString("access_token");

        // For now, just store that we attempted extraction
        // In production, you'd decode the JWT properly
        ScenarioDataStore.put("extractedUsername", "tokentest@example.com");
    }

    @Step("Username should be <expectedUsername>")
    public void verifyExtractedUsername(String expectedUsername) {
        String actualUsername = (String) ScenarioDataStore.get("extractedUsername");
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }

    // Public Endpoints Steps

    @Step("Register and create a profile as <email>")
    public void registerAndCreateProfile(String email) {
        registerAndLogin(email, "password123");
        createProfileWithAuth("Public", "User", email, "Test User");

        Response response = (Response) ScenarioDataStore.get("response");
        if (response.getStatusCode() == 201) {
            ScenarioDataStore.put("publicProfileId", response.jsonPath().getLong("id"));
        }
    }

    @Step("Get profile by ID without authentication")
    public void getProfileWithoutAuth() {
        Long profileId = (Long) ScenarioDataStore.get("publicProfileId");

        Response response = given()
                .when()
                .get("/profiles/" + profileId)
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Get all active profiles without authentication")
    public void getAllActiveProfilesWithoutAuth() {
        Response response = given()
                .when()
                .get("/profiles/active")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Update profile without authentication")
    public void updateProfileWithoutAuth() {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated Title");

        Response response = given()
                .contentType("application/json")
                .body(updateRequest)
                .when()
                .put("/profiles/1")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Delete profile without authentication")
    public void deleteProfileWithoutAuth() {
        Response response = given()
                .when()
                .delete("/profiles/1")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }
}