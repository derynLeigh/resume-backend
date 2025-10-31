# Profile API Specification

## Create Profile
* Start the application
* Create a new profile with first name "Deryn" and last name "Cullen"
* Profile should be created with status code "201"
* Profile email should be "deryn@example.com"

## Get Profile by ID
* Start the application
* Given a profile exists with email "deryn@example.com"
* When I request the profile by ID
* Then I should receive status code "200"
* And the response should contain first name "Deryn"

## Update Profile
* Start the application
* Given a profile exists with email "deryn@example.com"
* When I update the title to "Senior Technical Product Owner"
* Then I should receive status code "200"
* And the profile title should be "Senior Technical Product Owner"

## Delete Profile
* Start the application
* Given a profile exists with email "deryn@example.com"
* When I delete the profile
* Then I should receive status code "204"
* And the profile should not exist

## List Active Profiles
* Start the application
* Given multiple profiles exist with different active status
* When I request all active profiles
* Then I should receive status code "200"
* And I should only receive active profiles

## Add Experience to Profile
* Start the application
* Given a profile exists with email "deryn@example.com"
* When I add an experience with company "Sky" and title "Technical Product Owner"
* Then I should receive status code "201"
* And the profile should have "1" experience

## Add Multiple Skills to Profile
* Start the application
* Given a profile exists with email "deryn@example.com"
* When I add skills "Java, Spring Boot, React"
* Then I should receive status code "201"
* And the profile should have "3" skills

## Validate Profile Data
* Start the application
* When I try to create a profile without required fields
* Then I should receive status code "400"
* And the response should contain validation errors

## Profile with Complete Resume Data
* Start the application
* Given a profile exists with complete resume data
* When I request the full profile
* Then I should receive status code "200"
* And the response should contain experiences
* And the response should contain education
* And the response should contain skills
* And the response should contain certifications
