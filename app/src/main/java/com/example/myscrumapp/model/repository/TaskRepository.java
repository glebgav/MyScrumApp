package com.example.myscrumapp.model.repository;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.network.TasksApiService;
import com.example.myscrumapp.model.room.dao.TaskDao;
import com.example.myscrumapp.model.room.db.MyDatabase;
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
    private static final Long refreshTime = 5*60*100*1000*1000L;

    private final TaskDao taskDao;
    private MutableLiveData<List<Task>> allTasks  = new MutableLiveData<>();
    private MutableLiveData<Task> task = new MutableLiveData<>();
    private final TasksApiService tasksApiService;
    private final TaskRunner taskRunner = new TaskRunner();
    private final SharedPreferencesHelper preferencesHelper;
    private final CompositeDisposable disposable = new CompositeDisposable();


    public TaskRepository(Application application){
        MyDatabase database = MyDatabase.getInstance(application);
        preferencesHelper = SharedPreferencesHelper.getInstance(application);
        taskDao = database.taskDao();
        tasksApiService = new TasksApiService();
    }

    public MutableLiveData<List<Task>> getAllTasks(){
        Long updateTime = preferencesHelper.getUpdateTime();
        Long currentTime = System.nanoTime();
        if(updateTime != 0 && currentTime -updateTime < refreshTime) {
            return fetchFromDatabase();
        }else{
            return fetchFromRemote();
        }
    }

    public MutableLiveData<Task> getTask(String taskId){
        taskRunner.executeAsync(new getTaskByIdTask(taskDao, taskId), this::taskRetrieved);
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
        taskRunner.executeAsync(new getAllTasksFromLocalTask(taskDao), this::tasksRetrieved);
        return allTasks;
    }

    private MutableLiveData<List<Task>> fetchFromRemote() {
        disposable.add(
                tasksApiService.getTasks()
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
        public List<Task> call() throws Exception {
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

    private static class getAllTasksFromLocalTask implements Callable<List<Task>>{
        private final TaskDao taskDao;
        public getAllTasksFromLocalTask(TaskDao taskDao){
            this.taskDao = taskDao;
        }

        @Override
        public List<Task> call() throws Exception {
            return taskDao.getAllTasks();
        }
    }

    private static class getTaskByIdTask implements Callable<Task> {
        private final String taskId;
        private final TaskDao taskDao;

        public getTaskByIdTask(TaskDao taskDao, String taskId)
        {
            this.taskDao = taskDao;
            this.taskId = taskId;
        }

        @Override
        public Task call() throws Exception {
            return taskDao.getTaskByTaskId(taskId);
        }
    }





}
