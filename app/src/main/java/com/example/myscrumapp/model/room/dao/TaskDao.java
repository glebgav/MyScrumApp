package com.example.myscrumapp.model.room.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.myscrumapp.model.entity.Task;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    List<Long> insertAll(Task...tasks);

    @Insert
    void insert(Task task);

    @Update
    Void update(Task task);

    @Query("select * from tasks")
    List<Task> getAllTasks();

    @Query("select * from tasks where id= :id")
    Task getTask(int id);

    @Query("select * from tasks where taskId= :taskId")
    Task getTaskByTaskId(String taskId);

    @Query("select * from tasks where userId= :userId")
    List<Task> getTaskByUserId(String userId);

    @Query("select * from tasks where userId= :userId and status= :status")
    List<Task> getTaskByUserIdAndByStatus(String userId,int status);

    @Query("select * from tasks where teamId= :teamId")
    List<Task> getTaskByTeamId(String teamId);

    @Query("select * from tasks where teamId= :teamId and status= :status")
    List<Task> getTaskByTeamIdAndByStatus(String teamId,int status);

    @Query("delete from tasks")
    void deleteAllTasks();
}
