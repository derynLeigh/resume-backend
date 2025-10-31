
package com.deryncullen.resume.specs;

import com.deryncullen.resume.ResumeBackendApplication;
import com.thoughtworks.gauge.AfterSuite;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ProfileApiSteps {

    private static ConfigurableApplicationContext context;

    @Step("Start the application")
    public void startApplication() {
        if (context == null) {
            context = SpringApplication.run(ResumeBackendApplication.class,
                    "--server.port=8081",
                    "--spring.profiles.active=test");
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = 8081;
            RestAssured.basePath = "/api";
        }
    }

    @AfterSuite
    public void tearDown() {
        if (context != null && context.isActive()) {
            context.close();
            context = null;
        }
    }

    @Step("Create a new profile with first name <firstName> and last name <lastName>")
    public void createProfile(String firstName, String lastName) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", firstName);
        profile.put("lastName", lastName);
        // Generate unique email to avoid conflicts
        profile.put("email", firstName.toLowerCase() + "." + lastName.toLowerCase() +
                System.currentTimeMillis() + "@example.com");
        profile.put("title", "Technical Product Owner");
        profile.put("summary", "Dynamic technology professional...");

        Response response = given()
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

    @Step("Profile should be created with status code <statusCode>")
    public void verifyStatusCode(String statusCode) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(Integer.parseInt(statusCode));
    }

    @Step("Profile email should be <email>")
    public void verifyEmail(String email) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.jsonPath().getString("email")).isEqualTo(email);
    }

    @Step("Given a profile exists with email <email>")
    public void givenProfileExists(String email) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", "Deryn");
        profile.put("lastName", "Cullen");
        profile.put("email", email);
        profile.put("title", "Technical Product Owner");

        Response response = given()
                .contentType("application/json")
                .body(profile)
                .when()
                .post("/profiles")
                .then()
                .statusCode(201)
                .extract()
                .response();

        ScenarioDataStore.put("profileId", response.jsonPath().getLong("id"));
        ScenarioDataStore.put("profile", response.jsonPath().getMap(""));
    }

    @Step("When I request the profile by ID")
    public void requestProfileById() {
        Long profileId = (Long) ScenarioDataStore.get("profileId");
        if (profileId == null) {
            throw new IllegalStateException("Profile ID not found in ScenarioDataStore");
        }

        Response response = given()
                .when()
                .get("/profiles/" + profileId)
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("Then I should receive status code <statusCode>")
    public void thenVerifyStatusCode(String statusCode) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(Integer.parseInt(statusCode));
    }

    @Step("And the response should contain first name <firstName>")
    public void verifyFirstName(String firstName) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(firstName);
    }

    @Step("When I update the title to <title>")
    public void updateTitle(String title) {
        Long profileId = (Long) ScenarioDataStore.get("profileId");
        if (profileId == null) {
            throw new IllegalStateException("Profile ID not found in ScenarioDataStore");
        }

        Map<String, Object> profile = (Map<String, Object>) ScenarioDataStore.get("profile");
        if (profile == null) {
            throw new IllegalStateException("Profile data not found in ScenarioDataStore");
        }
        profile.put("title", title);

        Response response = given()
                .contentType("application/json")
                .body(profile)
                .when()
                .put("/profiles/" + profileId)
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("And the profile title should be <title>")
    public void verifyTitle(String title) {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        assertThat(response.jsonPath().getString("title")).isEqualTo(title);
    }

    @Step("When I delete the profile")
    public void deleteProfile() {
        Long profileId = (Long) ScenarioDataStore.get("profileId");
        if (profileId == null) {
            throw new IllegalStateException("Profile ID not found in ScenarioDataStore");
        }

        Response response = given()
                .when()
                .delete("/profiles/" + profileId)
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("And the profile should not exist")
    public void verifyProfileDoesNotExist() {
        Long profileId = (Long) ScenarioDataStore.get("profileId");
        if (profileId == null) {
            throw new IllegalStateException("Profile ID not found in ScenarioDataStore");
        }

        given()
                .when()
                .get("/profiles/" + profileId)
                .then()
                .statusCode(404);
    }

    @Step("Given multiple profiles exist with different active status")
    public void createMultipleProfiles() {
        // Create active profile
        Map<String, Object> activeProfile = new HashMap<>();
        activeProfile.put("firstName", "Active");
        activeProfile.put("lastName", "User");
        activeProfile.put("email", "active" + System.currentTimeMillis() + "@example.com");
        activeProfile.put("title", "Active Developer");
        activeProfile.put("active", true);

        given()
                .contentType("application/json")
                .body(activeProfile)
                .when()
                .post("/profiles")
                .then()
                .statusCode(201);

        // Create inactive profile
        Map<String, Object> inactiveProfile = new HashMap<>();
        inactiveProfile.put("firstName", "Inactive");
        inactiveProfile.put("lastName", "User");
        inactiveProfile.put("email", "inactive" + System.currentTimeMillis() + "@example.com");
        inactiveProfile.put("title", "Inactive Developer");
        inactiveProfile.put("active", false);

        given()
                .contentType("application/json")
                .body(inactiveProfile)
                .when()
                .post("/profiles")
                .then()
                .statusCode(201);
    }

    @Step("When I request all active profiles")
    public void requestActiveProfiles() {
        Response response = given()
                .when()
                .get("/profiles/active")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("And I should only receive active profiles")
    public void verifyOnlyActiveProfiles() {
        Response response = (Response) ScenarioDataStore.get("response");
        assertThat(response).isNotNull();
        response.jsonPath().getList("$")
                .forEach(profile -> {
                    Map<String, Object> p = (Map<String, Object>) profile;
                    assertThat(p.get("active")).isEqualTo(true);
                });
    }

    @Step("When I add an experience with company <company> and title <title>")
    public void addExperience(String company, String title) {
        Long profileId = (Long) ScenarioDataStore.get("profileId");
        if (profileId == null) {
            throw new IllegalStateException("Profile ID not found in ScenarioDataStore");
        }

        Map<String, Object> experience = new HashMap<>();
        experience.put("companyName", company);
        experience.put("jobTitle", title);
        experience.put("startDate", "2025-01-01");
        experience.put("current", true);

        Response response = given()
                .contentType("application/json")
                .body(experience)
                .when()
                .post("/profiles/" + profileId + "/experiences")
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("And the profile should have <count> experience")
    public void verifyExperienceCount(String count) {
        Long profileId = (Long) ScenarioDataStore.get("profileId");
        if (profileId == null) {
            throw new IllegalStateException("Profile ID not found in ScenarioDataStore");
        }

        Response response = given()
                .when()
                .get("/profiles/" + profileId)
                .then()
                .extract()
                .response();

        assertThat(response.jsonPath().getList("experiences")).hasSize(Integer.parseInt(count));
    }

    @Step("When I add skills <skills>")
    public void addSkills(String skills) {
        Long profileId = (Long) ScenarioDataStore.get("profileId");
        if (profileId == null) {
            throw new IllegalStateException("Profile ID not found in ScenarioDataStore");
        }

        String[] skillArray = skills.split(",");

        for (String skillName : skillArray) {
            Map<String, Object> skill = new HashMap<>();
            skill.put("name", skillName.trim());
            skill.put("category", "PROGRAMMING_LANGUAGE");
            skill.put("proficiencyLevel", "ADVANCED");

            Response skillResponse = given()
                    .contentType("application/json")
                    .body(skill)
                    .when()
                    .post("/profiles/" + profileId + "/skills")
                    .then()
                    .extract()
                    .response();

            // Verify each skill was added successfully
            assertThat(skillResponse.getStatusCode()).isEqualTo(201);
        }

        // Get the updated profile
        Response response = given()
                .when()
                .get("/profiles/" + profileId)
                .then()
                .extract()
                .response();

        ScenarioDataStore.put("response", response);
    }

    @Step("And the profile should have <count> skills")
    public void verifySkillCount(String count) {
        Long profileId = (Long) ScenarioDataStore.get("profileId");
        if (profileId == null) {
            throw new IllegalStateException("Profile ID not found in ScenarioDataStore");
        }

        Response response = given()
                .when()
                .get("/profiles/" + profileId)
                .then()
                .extract()
                .response();

        assertThat(response.jsonPath().getList("skills")).hasSize(Integer.parseInt(count));
    }
}