package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface TeamsApi {
    @GET("users/6YDBBl2TAH5SCJlIeX1A/teams")
    Single<List<Team>> getTeams(@Header("authorization") String token);
}
