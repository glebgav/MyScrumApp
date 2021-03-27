package com.example.myscrumapp.model.entity;


import java.util.ArrayList;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserRegisterDetails {
    public String userId;
    public String firstName;
    public String lastName;
    public String password;
    public String email;
    public Boolean isManager;
    public ArrayList<Team> teams;
    public ArrayList<Task> tasks;

    public UserRegisterDetails(String userId,String firstName,  String lastName, String password, String email, Boolean isManager, ArrayList<Team> teams, ArrayList<Task> tasks) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.isManager = isManager;
        this.teams = teams;
        this.tasks = tasks;
    }
}
