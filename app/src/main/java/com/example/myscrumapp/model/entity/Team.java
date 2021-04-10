package com.example.myscrumapp.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

import lombok.NoArgsConstructor;
/**
 * Model for team in local Room Db
 */
@NoArgsConstructor
@Entity(tableName = "teams")
public class Team {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "teamId")
    private String teamId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "isMyTeam")
    private boolean isMyTeam;

    @Ignore
    private ArrayList<Task> tasks;

    @Ignore
    private ArrayList<User> users;

    public Team(String teamId, String name, boolean isMyTeam) {
        this.teamId = teamId;
        this.name = name;
        this.isMyTeam = isMyTeam;
    }

    public Team(String teamId, String name,boolean isMyTeam, ArrayList<Task> tasks, ArrayList<User> users) {
        this.teamId = teamId;
        this.name = name;
        this.isMyTeam = isMyTeam;
        this.tasks = tasks;
        this.users = users;
    }

    public Team(String teamId, String name, ArrayList<Task> tasks, ArrayList<User> users) {
        this.teamId = teamId;
        this.name = name;
        this.tasks = tasks;
        this.users = users;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMyTeam() {
        return isMyTeam;
    }

    public void setMyTeam(boolean myTeam) {
        isMyTeam = myTeam;
    }
}
