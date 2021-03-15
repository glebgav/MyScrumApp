package com.example.myscrumapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.TeamToCreate;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.repository.TaskRepository;
import com.example.myscrumapp.model.repository.TeamRepository;
import com.example.myscrumapp.model.repository.UserRepository;

import java.util.List;

public class AddTeamViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> teamCreated;
    private final MutableLiveData<List<User>> users;
    private final MutableLiveData<List<Task>> tasks;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public AddTeamViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        teamRepository = new TeamRepository(application);
        taskRepository = new TaskRepository(application);
        users = userRepository.getAllUsers();
        tasks = taskRepository.getAllTasksFromRemote();
        teamCreated = teamRepository.getIsCreatedLiveData();
    }

    public void addTeam(TeamToCreate team){
        teamRepository.addTeam(team);
    }

    public  MutableLiveData<Boolean> getIsTeamCreated(){
        return teamCreated;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<List<Task>> getTasksLiveData() {
        return tasks;
    }

    public MutableLiveData<List<User>> getUsersLiveData() {
        return users;
    }


    public void setIsLoading(boolean value) {
        isLoading.setValue(value);
    }



}
