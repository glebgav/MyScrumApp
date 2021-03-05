package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Team;
import java.util.List;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface TeamsApi {
    @GET("users/{userId}/teams")
    Single<List<Team>> getTeamsByUserId(@Header("authorization") String token, @Path("userId") String userId);

}
