package com.example.myscrumapp.viewmodel;

import android.app.Application;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.repository.TaskRepository;
import java.util.List;

import lombok.Getter;

@Getter
public class ListViewModel extends AndroidViewModel {

    private MutableLiveData<List<Task>> tasks;
    private MutableLiveData<Boolean> taskLoadError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private TaskRepository taskRepository;

    public ListViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
        tasks = taskRepository.getAllTasks();

    }
    public  MutableLiveData<Boolean> getTaskLoadError(){
        return taskLoadError;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<List<Task>> getTasksLiveData() {
        return tasks;
    }


    public void refreshBypassCache(){
        taskRepository.refreshBypassCache();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }

}
