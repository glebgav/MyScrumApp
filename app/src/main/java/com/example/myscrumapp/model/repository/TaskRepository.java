package com.example.myscrumapp.model.repository;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.room.dao.TaskDao;
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
    private final MutableLiveData<List<Task>> allTasks  = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> myTasks  = new MutableLiveData<>();
    private final MutableLiveData<Task> task = new MutableLiveData<>();
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


    public MutableLiveData<List<Task>> getMyTasks(){
        Long updateTime = preferencesHelper.getUpdateTime();
        Long currentTime = System.nanoTime();
        if(updateTime != 0 && currentTime -updateTime < GlobalConstants.REFRESH_TIME) {
            return fetchMyTasksFromDatabase();
        }else{
            return fetchFromRemote();
        }
    }


    public MutableLiveData<List<Task>> getTasksByTeamId(String teamId){
        taskRunner.executeAsync(new GetTaskByTeamIdTask(taskDao, teamId), this::tasksRetrieved);
        return allTasks;
    }

    public MutableLiveData<List<Task>> getTasksByTeamIdAndStatus(String teamId, int status){
        taskRunner.executeAsync(new GetTaskByTeamIdAndStatusTask(taskDao, teamId, status), this::tasksRetrieved);
        return allTasks;
    }

    public MutableLiveData<List<Task>> getMyTasksByStatus(int status){
        LoggedInUser user = preferencesHelper.getUser();
        taskRunner.executeAsync(new GetTaskByUserIdAndStatusTask(taskDao, user.userId, status), this::myTasksRetrieved);
        return myTasks;
    }


    public MutableLiveData<Task> getTask(String taskId){
        taskRunner.executeAsync(new GetTaskByIdTask(taskDao, taskId), this::taskRetrieved);
        return task;
    }

    public void tasksRetrieved(List<Task> tasksList){
        allTasks.setValue(tasksList);
    }

    public void myTasksRetrieved(List<Task> tasksList){
        myTasks.setValue(tasksList);
    }

    public void taskRetrieved(Task task){
        this.task.setValue(task);
    }


    public void refreshBypassCache(){
        fetchFromRemote();
    }


    private MutableLiveData<List<Task>> fetchAllTasksFromDatabase(){
        taskRunner.executeAsync(new GetAllTasksFromLocalTask(taskDao), this::tasksRetrieved);
        return allTasks;
    }

    public MutableLiveData<List<Task>> fetchMyTasksFromDatabase(){

        LoggedInUser user = preferencesHelper.getUser();
        taskRunner.executeAsync(new GetTaskByUserIdTask(taskDao, user.userId), this::myTasksRetrieved);
        return myTasks;
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
                                       // tasksRetrieved(data);
                                        preferencesHelper.saveUpdateTime(System.nanoTime());
                                        fetchMyTasksFromDatabase();
                                    });
                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                    e.printStackTrace();
                                }
                            })

            );
        return myTasks;
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

    private static class GetTaskByUserIdTask implements Callable<List<Task>> {
        private final String userId;
        private final TaskDao taskDao;

        public GetTaskByUserIdTask(TaskDao taskDao, String userId)
        {
            this.taskDao = taskDao;
            this.userId = userId;
        }

        @Override
        public List<Task> call() {
            return taskDao.getTaskByUserId(userId);
        }
    }

    private static class GetTaskByUserIdAndStatusTask implements Callable<List<Task>> {
        private final String userId;
        private final TaskDao taskDao;
        private final int status;

        public GetTaskByUserIdAndStatusTask(TaskDao taskDao, String userId, int status)
        {
            this.taskDao = taskDao;
            this.userId = userId;
            this.status = status;
        }

        @Override
        public List<Task> call() {
            return taskDao.getTaskByUserIdAndByStatus(userId, status);
        }
    }

    private static class GetTaskByTeamIdTask implements Callable<List<Task>> {
        private final String teamId;
        private final TaskDao taskDao;

        public GetTaskByTeamIdTask(TaskDao taskDao, String teamId)
        {
            this.taskDao = taskDao;
            this.teamId = teamId;

        }

        @Override
        public List<Task> call() {
            return taskDao.getTaskByTeamId(teamId);
        }
    }

    private static class GetTaskByTeamIdAndStatusTask implements Callable<List<Task>> {
        private final String teamId;
        private final TaskDao taskDao;
        private final int status;

        public GetTaskByTeamIdAndStatusTask(TaskDao taskDao, String teamId, int status)
        {
            this.taskDao = taskDao;
            this.teamId = teamId;
            this.status = status;
        }

        @Override
        public List<Task> call() {
            return taskDao.getTaskByTeamIdAndByStatus(teamId,status);
        }
    }





}
