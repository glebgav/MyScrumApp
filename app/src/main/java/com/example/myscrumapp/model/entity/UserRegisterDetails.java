package com.example.myscrumapp.model.entity;


public class UserRegisterDetails {
    public String firstName;
    public String lastName;
    public String password;
    public String email;
    public Boolean isManager;

    public UserRegisterDetails(String firstName, String lastName, String password, String email, Boolean isManager) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.isManager = isManager;
    }
}
