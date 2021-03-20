package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.TeamToCreate;

import java.util.List;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TasksApi {
    @GET("users/{userId}/teams/tasks")
    Single<List<Task>> getTeamsTasks(@Header("authorization") String token,@Path("userId") String userId);

    @GET("users/{userId}/tasks")
    Single<List<Task>> getMyTasks(@Header("authorization") String token,@Path("userId") String userId);

    @PUT("tasks/{taskId}")
    Single<Task> updateTask(@Header("authorization") String token,@Path("taskId") String taskId, @Body Task task);

    @POST("tasks")
    Single<Task> createTask(@Header("authorization") String token, @Body Task task);

    @DELETE("tasks/{taskId}")
    Single<OperationResponseModel> deleteTask(@Header("authorization") String token,@Path("taskId") String taskId);
}
