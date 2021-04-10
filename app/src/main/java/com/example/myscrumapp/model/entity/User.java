package com.example.myscrumapp.model.entity;
/**
 * Model for user (lightweight version) in task in local Room Db
 */
public class User {
    public String userId;
    public String firstName;
    public String lastName;

    public User(String userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
