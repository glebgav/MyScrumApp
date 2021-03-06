package com.example.myscrumapp.model.entity;

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
