package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Task;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class TasksApiService {
    private static final String BASE_URL = "http://192.168.1.10:8080";

    private TasksApi tasksApi;

    public TasksApiService(){
        tasksApi = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(TasksApi.class);
    }

    public Single<List<Task>> getTasks(){
        return tasksApi.getTasks("Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmYWZhQGdtYWlsLmNvbSIsImV4cCI6MTYxNTQ5OTQwMn0.HzizWqZAeR42etXrY0XttfNZluBTn5vnHSm9EHPiojIcmW7iemQDaLERFdahZpNPzE5Kne8hHC7ao3HZVAFGPg");
    }
}
