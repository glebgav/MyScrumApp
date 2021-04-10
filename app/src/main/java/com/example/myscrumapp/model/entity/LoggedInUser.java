package com.example.myscrumapp.model.entity;

/**
 * Model for logged in user details that are stored in shared preferences with SharedPreferencesHelper class
 */
public class LoggedInUser {
    public String firstName;
    public String userId;
    public Boolean isManager;
    public String email;
    public String token;

    public LoggedInUser(String firstName, String userId, String email, String token, Boolean isManager) {
        this.firstName = firstName;
        this.userId = userId;
        this.isManager = isManager;
        this.email = email;
        this.token = token;
    }
}
