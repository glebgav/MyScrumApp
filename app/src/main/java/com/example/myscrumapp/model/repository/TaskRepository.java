package com.example.myscrumapp.model.repository;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.room.dao.TaskDao;
import com.example.myscrumapp.model.room.dao.TeamDao;
import com.example.myscrumapp.model.room.db.MyDatabase;
import com.example.myscrumapp.utils.GlobalConstants;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.example.myscrumapp.utils.TaskRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class TaskRepository {

    private final TaskDao taskDao;
    private MutableLiveData<List<Task>> allTasks  = new MutableLiveData<>();
    private MutableLiveData<Task> task = new MutableLiveData<>();
    private final ApiService apiService;
    private final TaskRunner taskRunner = new TaskRunner();
    private final SharedPreferencesHelper preferencesHelper;
    private final CompositeDisposable disposable = new CompositeDisposable();


    public TaskRepository(Application application){
        MyDatabase database = MyDatabase.getInstance(application);
        preferencesHelper = SharedPreferencesHelper.getInstance(application);
        taskDao = database.taskDao();
        apiService = ApiService.getInstance();
    }


    public MutableLiveData<List<Task>> getAllTasks(){
        Long updateTime = preferencesHelper.getUpdateTime();
        Long currentTime = System.nanoTime();
        if(updateTime != 0 && currentTime -updateTime < GlobalConstants.REFRESH_TIME) {
            return fetchFromDatabase();
        }else{
            return fetchFromRemote();
        }
    }

    public MutableLiveData<Task> getTask(String taskId){
        taskRunner.executeAsync(new GetTaskByIdTask(taskDao, taskId), this::taskRetrieved);
        return task;
    }

    public void tasksRetrieved(List<Task> tasksList){
        allTasks.setValue(tasksList);
    }

    public void taskRetrieved(Task task){
        this.task.setValue(task);
    }


    public void refreshBypassCache(){
        fetchFromRemote();
    }


    private MutableLiveData<List<Task>> fetchFromDatabase(){
        taskRunner.executeAsync(new GetAllTasksFromLocalTask(taskDao), this::tasksRetrieved);
        return allTasks;
    }


    private MutableLiveData<List<Task>> fetchFromRemote() {
        LoggedInUser user = preferencesHelper.getUser();
            disposable.add(
                    apiService.getTasksApi().getTeamsTasks(user.token,user.userId)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<List<Task>>() {
                                @Override
                                public void onSuccess(@io.reactivex.annotations.NonNull List<Task> tasksList) {
                                    taskRunner.executeAsync(new InsertTasksFromRemoteToLocalTask(taskDao, tasksList), (data) ->{
                                        tasksRetrieved(data);
                                        preferencesHelper.saveUpdateTime(System.nanoTime());
                                    });
                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                    e.printStackTrace();
                                }
                            })

            );
        return allTasks;
    }



    private static class InsertTasksFromRemoteToLocalTask implements Callable<List<Task>> {

        private final List<Task>[] lists;
        private final TaskDao taskDao;

        @SafeVarargs
        private InsertTasksFromRemoteToLocalTask(TaskDao taskDao, List<Task>... lists) {
            this.taskDao = taskDao;
            this.lists = lists;
        }

        @Override
        public List<Task> call() {
            List<Task> list = lists[0];
            taskDao.deleteAllTasks();

            ArrayList<Task> newList = new ArrayList<>(list);
            List<Long> result = taskDao.insertAll(newList.toArray(new Task[0]));

            int i=0;
            while (i<list.size()) {
                list.get(i).setId(result.get(i).intValue());
                ++i;
            }

            return list;
        }
    }

    private static class GetAllTasksFromLocalTask implements Callable<List<Task>>{
        private final TaskDao taskDao;
        public GetAllTasksFromLocalTask(TaskDao taskDao){
            this.taskDao = taskDao;
        }

        @Override
        public List<Task> call() {
            return taskDao.getAllTasks();
        }
    }

    private static class GetTaskByIdTask implements Callable<Task> {
        private final String taskId;
        private final TaskDao taskDao;

        public GetTaskByIdTask(TaskDao taskDao, String taskId)
        {
            this.taskDao = taskDao;
            this.taskId = taskId;
        }

        @Override
        public Task call() {
            return taskDao.getTaskByTaskId(taskId);
        }
    }





}
