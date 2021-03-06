package com.example.myscrumapp.model.room.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myscrumapp.model.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    List<Long> insertAll(Task...tasks);

    @Query("select * from tasks")
    List<Task> getAllTasks();

    @Query("select * from tasks where id= :id")
    Task getTask(int id);

    @Query("select * from tasks where taskId= :taskId")
    Task getTaskByTaskId(String taskId);

    @Query("select * from tasks where userId= :userId")
    List<Task> getTaskByUserId(String userId);

    @Query("select * from tasks where teamId= :teamId")
    List<Task> getTaskByTeamId(String teamId);

    @Query("delete from tasks")
    void deleteAllTasks();
}
