package com.example.myscrumapp.model.entity;

public class LoggedInUser {
    public String firstName;
    public String userId;
    public String email;
    public String token;

    public LoggedInUser(String firstName, String userId, String email, String token) {
        this.firstName = firstName;
        this.userId = userId;
        this.email = email;
        this.token = token;
    }
}
