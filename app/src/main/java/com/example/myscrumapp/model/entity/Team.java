package com.example.myscrumapp.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;



@Entity(tableName = "teams")
public class Team {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "teamId")
    public String teamId;

    @ColumnInfo(name = "name")
    public String name;

}
