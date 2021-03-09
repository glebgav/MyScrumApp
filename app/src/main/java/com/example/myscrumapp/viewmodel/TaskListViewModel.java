package com.example.myscrumapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import lombok.Getter;

@Getter
public class TaskListViewModel extends AndroidViewModel {

    private final LiveData<List<Task>> tasks;
    private MutableLiveData<List<Task>> myTasks;
    private final MutableLiveData<String> teamIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> taskLoadError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final TaskRepository taskRepository;

    public TaskListViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);

        tasks = Transformations.switchMap(teamIdLiveData, taskRepository::getTasksByTeamId);
        myTasks = taskRepository.getMyTasks();
    }

    public void setToMyTasks(){
        myTasks = taskRepository.getMyTasks();
    }

    public void setTeamIdLiveData(String teamId){
        teamIdLiveData.setValue(teamId);
    }

    public void update(Task task){
        taskRepository.update(task);
    }

    public void filterTeamTasksByStatus(String teamId, int status){
        taskRepository.getTasksByTeamIdAndStatus(teamId, status);
    }

    public void filterMyTasksByStatus(int status){
        taskRepository.getMyTasksByStatus(status);
    }


    public  MutableLiveData<Boolean> getTaskLoadError(){
        return taskLoadError;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<Task>> getTasksLiveData() {
        return tasks;
    }

    public MutableLiveData<List<Task>> getMyTasksLiveData() {
        return myTasks;
    }


    public void refreshBypassCache(){
        taskRepository.refreshBypassCache();
    }

}
