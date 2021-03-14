package com.example.myscrumapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.repository.TaskRepository;
import com.example.myscrumapp.model.repository.TeamRepository;
import com.example.myscrumapp.model.repository.UserRepository;

import java.util.List;

public class AddUserViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> userCreated;
    private final MutableLiveData<List<Team>> teams;
    private final MutableLiveData<List<Task>> tasks;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public AddUserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        teamRepository = new TeamRepository(application);
        taskRepository = new TaskRepository(application);
        teams = teamRepository.getAllTeamsFromRemote();
        tasks = taskRepository.getAllTasksFromRemote();
        userCreated = userRepository.getIsCreatedLiveData();
    }

    public void addUser(UserRegisterDetails user){
        userRepository.addUser(user);
    }

    public  MutableLiveData<Boolean> getIsUserCreated(){
        return userCreated;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<List<Team>> getTeamsLiveData() {
        return teams;
    }

    public MutableLiveData<List<Task>> getTasksLiveData() {
        return tasks;
    }

    public void setIsLoading(boolean value) {
        isLoading.setValue(value);
    }



}
