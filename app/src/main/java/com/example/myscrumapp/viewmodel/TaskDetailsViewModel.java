package com.example.myscrumapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.room.db.TaskDatabase;
import com.example.myscrumapp.utils.TaskRunner;
import java.util.concurrent.Callable;

import lombok.Getter;

@Getter
public class TaskDetailsViewModel extends AndroidViewModel {
    private MutableLiveData<Task> taskMutableLiveData = new MutableLiveData<>();
    private final TaskRunner taskRunner = new TaskRunner();

    public TaskDetailsViewModel(@NonNull Application application) {
        super(application);
    }

    public void fetch(String taskId){
        fetchFromDatabase(taskId);
    }

    private void fetchFromDatabase(String taskId){
        taskRunner.executeAsync(new TaskDetailsViewModel.RetrieveTaskTask(taskId), this::taskRetrieved);
    }

    private void taskRetrieved(Task task){
        taskMutableLiveData.setValue(task);
    }

    private class RetrieveTaskTask implements Callable<Task> {
        private final String taskId;
        public RetrieveTaskTask(String taskId){
            this.taskId = taskId;
        }

        @Override
        public Task call() throws Exception {
            return TaskDatabase.getInstance(getApplication()).taskDao().getTaskByTaskId(taskId);
        }
    }
}
