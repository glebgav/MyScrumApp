package com.example.myscrumapp.model.room.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.room.dao.TaskDao;

@Database(entities = Task.class,version = 1)
public abstract class TaskDatabase extends RoomDatabase {
    private static TaskDatabase instance = null;

    public static TaskDatabase getInstance(Context context){
        if(instance == null){
            instance  = Room.databaseBuilder(
                    context.getApplicationContext(),
                    TaskDatabase.class,
                    "tasksdatabase")
                    .build();
        }
        return instance;
    }

    public abstract TaskDao taskDao();
}
