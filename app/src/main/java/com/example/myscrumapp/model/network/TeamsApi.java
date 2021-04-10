package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Team;
import java.util.List;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 *network api interface for teams
 */
public interface TeamsApi {
    @GET("users/{userId}/teams")
    Single<List<Team>> getTeamsByUserId(@Header("authorization") String token, @Path("userId") String userId);

    @GET("teams")
    Single<List<Team>> getAllTeams(@Header("authorization") String token, @Query("page")int page, @Query("limit") int limit);

    @POST("teams")
    Single<Team> createTeam(@Header("authorization") String token, @Body Team team);

    @PUT("teams/{teamId}")
    Single<Team> updateTeam(@Header("authorization") String token, @Path("teamId") String teamId, @Body Team team);

    @DELETE("teams/{teamId}")
    Single<OperationResponseModel> deleteTeam(@Header("authorization") String token,@Path("teamId") String teamId);

}
