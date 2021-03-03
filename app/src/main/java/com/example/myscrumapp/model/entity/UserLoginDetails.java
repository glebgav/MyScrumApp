package com.example.myscrumapp.model.entity;

public class UserLoginDetails {
    public String password;
    public String email;

    public UserLoginDetails(String password, String email) {
        this.password = password;
        this.email = email;
    }
}
