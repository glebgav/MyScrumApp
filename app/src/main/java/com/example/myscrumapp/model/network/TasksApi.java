package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Task;

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
 *network api interface for tasks
 */
public interface TasksApi {
    @GET("tasks")
    Single<List<Task>> getAllTasks(@Header("authorization") String token, @Query("page")int page, @Query("limit") int limit);

    @PUT("tasks/{taskId}")
    Single<Task> updateTask(@Header("authorization") String token,@Path("taskId") String taskId, @Body Task task);

    @POST("tasks")
    Single<Task> createTask(@Header("authorization") String token, @Body Task task);

    @DELETE("tasks/{taskId}")
    Single<OperationResponseModel> deleteTask(@Header("authorization") String token,@Path("taskId") String taskId);
}
