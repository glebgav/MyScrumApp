package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {
    private static final String BASE_URL = "http://192.168.1.44:8080";

    private static  ApiService instance;
    private final Retrofit retrofit;

    private ApiService(){
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
    public static synchronized  ApiService getInstance(){
        if(instance == null)
            instance = new ApiService();
        return instance;
    }

    public TasksApi getTasksApi(){
        return retrofit.create(TasksApi.class);
    }

    public UsersApi getUsersApi(){
        return retrofit.create(UsersApi.class);
    }

    public TeamsApi getTeamsApi(){
        return retrofit.create(TeamsApi.class);
    }



}
