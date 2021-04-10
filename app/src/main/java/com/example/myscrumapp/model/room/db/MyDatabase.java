package com.example.myscrumapp.model.room.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.room.dao.TaskDao;
import com.example.myscrumapp.model.room.dao.TeamDao;
/**
 * database ORM declaration class
 */
@Database(entities = {Task.class, Team.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    private static MyDatabase instance = null;

    public static synchronized MyDatabase getInstance(Context context){
        if(instance == null){
            instance  = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MyDatabase.class,
                    "mydatabase")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract TaskDao taskDao();
    public abstract TeamDao teamDao();
}
