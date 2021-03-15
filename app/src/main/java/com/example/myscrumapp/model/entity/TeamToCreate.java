package com.example.myscrumapp.model.entity;

import java.util.ArrayList;

public class TeamToCreate {
    public String name;
    public ArrayList<Task> tasks;
    public ArrayList<User> users;

    public TeamToCreate(String name, ArrayList<Task> tasks, ArrayList<User> users) {
        this.name = name;
        this.tasks = tasks;
        this.users = users;
    }


}
