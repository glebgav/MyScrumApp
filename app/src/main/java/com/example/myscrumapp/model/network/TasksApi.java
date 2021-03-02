package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Task;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface TasksApi {
    @GET("users/6YDBBl2TAH5SCJlIeX1A/tasks")
    Single<List<Task>> getTasks(@Header("authorization") String token);
}
