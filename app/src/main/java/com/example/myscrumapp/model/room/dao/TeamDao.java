package com.example.myscrumapp.model.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import java.util.List;
/**
 * data access object for teams
 */
@Dao
public interface TeamDao {
    @Insert
    List<Long> insertAll(Team...teams);

    @Update
    Void update(Team team);

    @Delete
    Void delete(Team team);

    @Query("select * from teams")
    List<Team> getAllTeams();

    @Query("select * from teams where id= :id")
    Team getTeam(int id);

    @Query("select * from teams where teamId= :teamId")
    Team getTeamByTeamId(String teamId);

    @Query("select * from teams where isMyTeam=1")
    List<Team> getMyTeams();

    @Query("delete from teams")
    void deleteAllTeams();
}
