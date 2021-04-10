package com.example.myscrumapp.model.network;

import com.example.myscrumapp.utils.GlobalConstants;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class for instantiating the retrofit networking library and underling http client
 * and a single source for all network APIs
 */
public class ApiService {
    private static final String BASE_URL = GlobalConstants.API_SERVICE_URL;

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
