# Authentication API Specification

## Register New User
* Start the application
* Register user with email "newuser@example.com", password "SecurePass123", first name "John", and last name "Doe"
* Registration should return status code "201"
* Response should contain access token
* Response should contain refresh token
* Response should contain token type "Bearer"
* Access token should be valid JWT

## Register User with Duplicate Email
* Start the application
* Register user with email "duplicate@example.com", password "password123", first name "First", and last name "User"
* Registration should return status code "201"
* Register user with email "duplicate@example.com", password "password123", first name "Second", and last name "User"
* Registration should return status code "400"
* Response should contain error message about duplicate email

## Register User with Invalid Data
* Start the application
* Register user with empty email and password "password123"
* Registration should return status code "400"
* Response should contain validation errors
* Register user with email "invalid" and password "short"
* Registration should return status code "400"
* Response should contain validation errors

## Login with Valid Credentials
* Start the application
* Register user with email "login@example.com", password "MyPassword123", first name "Login", and last name "Test"
* Login with email "login@example.com" and password "MyPassword123"
* Login should return status code "200"
* Response should contain access token
* Response should contain refresh token

## Login with Invalid Credentials
* Start the application
* Register user with email "user@example.com", password "CorrectPass123", first name "User", and last name "Test"
* Login with email "user@example.com" and password "WrongPassword"
* Login should return status code "401"
* Login with email "nonexistent@example.com" and password "password123"
* Login should return status code "401"

## Access Protected Endpoint Without Token
* Start the application
* Create profile without authentication
* Request should return status code "401"

## Access Protected Endpoint With Valid Token
* Start the application
* Register and login as "authenticated@example.com" with password "SecurePass123"
* Create profile with first name "John", last name "Doe", email "john.doe@example.com", and title "Software Engineer"
* Profile creation should return status code "201"
* Profile should be created successfully

## Access Protected Endpoint With Invalid Token
* Start the application
* Create profile with invalid JWT token
* Request should return status code "401"

## Access Protected Endpoint With Expired Token
* Start the application
* Register and login as "expired@example.com" with password "SecurePass123"
* Wait for token to expire
* Create profile with expired token
* Request should return status code "401"

## Refresh Access Token with Valid Refresh Token
* Start the application
* Register and login as "refresh@example.com" with password "SecurePass123"
* Refresh access token using refresh token
* Refresh should return status code "200"
* Response should contain new access token
* Response should contain same refresh token
* New access token should be different from original

## Refresh Access Token with Invalid Refresh Token
* Start the application
* Refresh access token with invalid token
* Refresh should return status code "401"

## Token Contains Correct User Information
* Start the application
* Register user with email "tokentest@example.com", password "password123", first name "Token", and last name "Test"
* Login with email "tokentest@example.com" and password "password123"
* Extract username from access token
* Username should be "tokentest@example.com"

## Public Endpoints Accessible Without Authentication
* Start the application
* Register and create a profile as "public@example.com"
* Get profile by ID without authentication
* Request should return status code "200"
* Get all active profiles without authentication
* Request should return status code "200"

## Protected Endpoints Require Authentication
* Start the application
* Update profile without authentication
* Request should return status code "401"
* Delete profile without authentication
* Request should return status code "401"
