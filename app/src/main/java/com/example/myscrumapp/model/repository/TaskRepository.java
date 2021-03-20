package com.example.myscrumapp.model.repository;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.network.OperationResponseModel;
import com.example.myscrumapp.model.room.dao.TaskDao;
import com.example.myscrumapp.model.room.db.MyDatabase;
import com.example.myscrumapp.utils.GlobalConstants;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.example.myscrumapp.utils.TaskRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class TaskRepository {

    private final TaskDao taskDao;
    private final MutableLiveData<List<Task>> allTasks  = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> teamTasks  = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> myTasks  = new MutableLiveData<>();
    private final MutableLiveData<Task> task = new MutableLiveData<>();
    private final MutableLiveData<Boolean> taskIsCreated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> taskIsDeleted = new MutableLiveData<>();
    private final MutableLiveData<Boolean> taskIsUpdated = new MutableLiveData<>();
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

    public MutableLiveData<List<Task>> getAllTasksFromRemote(){
        return fetchFromRemote();
    }

    private MutableLiveData<List<Task>> getAllTasksFromLocal(){
        taskRunner.executeAsync(new GetAllTasksFromLocalTask(taskDao), this::tasksRetrieved);
        return allTasks;
    }


    public MutableLiveData<List<Task>> getTasksByTeamId(String teamId){
        taskRunner.executeAsync(new GetTaskByTeamIdTask(taskDao, teamId), this::teamTasksRetrieved);
        return teamTasks;
    }

    public void getTasksByTeamIdAndStatus(String teamId, int status){
        taskRunner.executeAsync(new GetTaskByTeamIdAndStatusTask(taskDao, teamId, status), this::teamTasksRetrieved);
    }

    public void getMyTasksByStatus(int status){
        LoggedInUser user = preferencesHelper.getUser();
        taskRunner.executeAsync(new GetTaskByUserIdAndStatusTask(taskDao, user.userId, status), this::myTasksRetrieved);
    }


    public MutableLiveData<Task> getTask(String taskId){
        taskRunner.executeAsync(new GetTaskByIdTask(taskDao, taskId), this::taskRetrieved);
        return task;
    }

    public void update(Task task){
        taskRunner.executeAsync(new UpdateTaskInLocalTask(taskDao, task), result -> updateInRemote(task));
    }


    public void updateInRemote(Task task){
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTasksApi().updateTask(user.token,task.getTaskId(),task)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Task>() {
                            @Override
                            public void onSuccess(@NonNull Task task) {
                                setIsUpdatedLiveData(true);
                            }
                            @Override
                            public void onError(@NonNull Throwable e) {
                                e.printStackTrace();
                            }
                        })
        );
    }

    public void addTask(Task task){
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTasksApi().createTask(user.token, task)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Task>() {
                            @Override
                            public void onSuccess(@NonNull Task task) {
                                setIsCreatedLiveData(true);
                            }
                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsCreatedLiveData(false);
                                e.printStackTrace();
                            }
                        })
        );
    }

    public void deleteTask(Task task){
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTasksApi().deleteTask(user.token, task.getTaskId())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<OperationResponseModel>() {
                            @Override
                            public void onSuccess(@NonNull OperationResponseModel operationResponseModel) {
                                setIsDeletedLiveData(true);
                                taskRunner.executeAsync(new DeleteTaskInLocalTask(taskDao, task), (data) ->{});
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsDeletedLiveData(false);
                                e.printStackTrace();
                            }
                        })
        );
    }

    public void tasksRetrieved(List<Task> tasksList){
        allTasks.setValue(tasksList);
    }

    public void teamTasksRetrieved(List<Task> tasksList){
        teamTasks.setValue(tasksList);
    }

    public void myTasksRetrieved(List<Task> tasksList){
        myTasks.setValue(tasksList);
    }

    public void taskRetrieved(Task task){
        this.task.setValue(task);
    }

    public void setIsCreatedLiveData(Boolean value){
        taskIsCreated.setValue(value);
    }

    public MutableLiveData<Boolean> getIsCreatedLiveData(){
        return taskIsCreated;
    }

    public void setIsUpdatedLiveData(Boolean value){
        taskIsUpdated.setValue(value);
    }

    public MutableLiveData<Boolean> getIsUpdatedLiveData(){
        return taskIsUpdated;
    }

    public void setIsDeletedLiveData(Boolean value){
        taskIsDeleted.setValue(value);
    }

    public MutableLiveData<Boolean> getIsDeletedLiveData(){
        return taskIsDeleted;
    }


    public void refreshBypassCache(){
        fetchFromRemote();
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
                                        preferencesHelper.saveUpdateTime(System.nanoTime());
                                        tasksRetrieved(data);
                                        fetchMyTasksFromDatabase();
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

    private static class UpdateTaskInLocalTask implements Callable<Void> {
        private final TaskDao taskDao;
        private final Task task;

        public UpdateTaskInLocalTask(TaskDao taskDao, Task task)
        {
            this.taskDao = taskDao;
            this.task = task;
        }

        @Override
        public Void call() {
            return taskDao.update(task);
        }
    }

    private static class DeleteTaskInLocalTask implements Callable<Void> {
        private final TaskDao taskDao;
        private final Task task;

        public DeleteTaskInLocalTask(TaskDao taskDao, Task task)
        {
            this.taskDao = taskDao;
            this.task = task;
        }

        @Override
        public Void call() {
            return taskDao.delete(task);
        }
    }





}
