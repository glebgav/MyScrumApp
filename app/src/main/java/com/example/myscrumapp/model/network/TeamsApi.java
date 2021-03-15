package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.TeamToCreate;
import com.example.myscrumapp.model.entity.UserRegisterDetails;

import java.util.List;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TeamsApi {
    @GET("users/{userId}/teams")
    Single<List<Team>> getTeamsByUserId(@Header("authorization") String token, @Path("userId") String userId);

    @POST("teams")
    Single<TeamToCreate> createTeam(@Header("authorization") String token, @Body TeamToCreate team);

}
