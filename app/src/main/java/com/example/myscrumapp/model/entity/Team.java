package com.example.myscrumapp.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;



@Entity(tableName = "teams")
public class Team {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "teamId")
    private String teamId;

    @ColumnInfo(name = "name")
    private String name;

    public Team(String teamId, String name) {
        this.teamId = teamId;
        this.name = name;
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
}
