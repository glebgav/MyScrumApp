package com.example.myscrumapp.model.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.myscrumapp.model.entity.Team;
import java.util.List;

@Dao
public interface TeamDao {
    @Insert
    List<Long> insertAll(Team...teams);

    @Query("select * from teams")
    List<Team> getAllTeams();

    @Query("select * from teams where id= :id")
    Team getTeam(int id);

    @Query("select * from teams where teamId= :teamId")
    Team getTeamByTeamId(String teamId);

    @Query("delete from teams")
    void deleteAllTeams();
}
