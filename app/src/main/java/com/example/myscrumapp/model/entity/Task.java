package com.example.myscrumapp.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.Setter;


@Entity(tableName = "tasks")
@Getter
@Setter
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "taskId")
    private String taskId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    private int status;

    @Embedded
    private User userDetails;

    @Embedded
    private TeamInTask teamDetails;

    public Task(String taskId, String title, String description, int status, User userDetails, TeamInTask teamDetails) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.userDetails = userDetails;
        this.teamDetails = teamDetails;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public User getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(User userDetails) {
        this.userDetails = userDetails;
    }

    public TeamInTask getTeamDetails() {
        return teamDetails;
    }

    public void setTeamDetails(TeamInTask teamDetails) {
        this.teamDetails = teamDetails;
    }
}
