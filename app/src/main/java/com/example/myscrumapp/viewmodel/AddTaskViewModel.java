package com.example.myscrumapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.model.network.OperationResponseModel;
import com.example.myscrumapp.model.repository.TaskRepository;
import com.example.myscrumapp.model.repository.TeamRepository;
import com.example.myscrumapp.model.repository.UserRepository;

import java.util.List;

public class AddTaskViewModel extends AndroidViewModel {
    private final MutableLiveData<OperationResponseModel> taskCreated;
    private final MutableLiveData<OperationResponseModel> taskDeleted;
    private final MutableLiveData<OperationResponseModel> taskUpdated;
    private final MutableLiveData<List<Task>> tasks;
    private final MutableLiveData<List<User>> users;
    private final MutableLiveData<List<Team>> teams;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public AddTaskViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        teamRepository = new TeamRepository(application);
        taskRepository = new TaskRepository(application);
        users = userRepository.getAllUsers();
        teams = teamRepository.getAllTeams();
        tasks = taskRepository.getAllTasksFromRemote();
        taskCreated = taskRepository.getIsCreatedLiveData();
        taskDeleted = taskRepository.getIsDeletedLiveData();
        taskUpdated = taskRepository.getIsUpdatedLiveData();
    }

    public void addTask(Task task){
        taskRepository.addTask(task);
    }

    public void updateTask(Task task){
        taskRepository.updateTask(task);
    }

    public void deleteTask(Task task){
        taskRepository.deleteTask(task);
    }

    public void refreshTasks(){
        taskRepository.getAllTasksFromRemote();
    }

    public  MutableLiveData<OperationResponseModel> getIsTaskCreated(){
        return taskCreated;
    }

    public  MutableLiveData<OperationResponseModel> getIsTaskDeleted(){
        return taskDeleted;
    }

    public  MutableLiveData<OperationResponseModel> getIsTaskUpdated(){
        return taskUpdated;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<List<Team>> getTeamsLiveData() {
        return teams;
    }

    public MutableLiveData<List<User>> getUsersLiveData() {
        return users;
    }

    public MutableLiveData<List<Task>> getTasksLiveData() {
        return tasks;
    }


    public void setIsLoading(boolean value) {
        isLoading.setValue(value);
    }



}
