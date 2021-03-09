package com.example.myscrumapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.repository.TaskRepository;
import lombok.Getter;


@Getter
public class TaskDetailsViewModel extends AndroidViewModel {
    private MutableLiveData<Task> taskMutableLiveData;
    private final TaskRepository taskRepository;

    public TaskDetailsViewModel(@NonNull Application application) {
        super(application);
        taskRepository =  new TaskRepository(application);
    }

    public void getTask(String taskId){
        taskMutableLiveData = taskRepository.getTask(taskId);
    }

}
