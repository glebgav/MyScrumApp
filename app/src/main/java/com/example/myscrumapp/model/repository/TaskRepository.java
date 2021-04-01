package com.example.myscrumapp.model.repository;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.network.OperationResponseModel;
import com.example.myscrumapp.model.network.OperationResponseStatus;
import com.example.myscrumapp.model.room.dao.TaskDao;
import com.example.myscrumapp.model.room.db.MyDatabase;
import com.example.myscrumapp.utils.GlobalConstants;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.example.myscrumapp.utils.TaskRunner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;
import retrofit2.HttpException;

public class TaskRepository {

    private final TaskDao taskDao;
    private final MutableLiveData<List<Task>> allTasks  = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> teamTasks  = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> myTasks  = new MutableLiveData<>();
    private final MutableLiveData<Task> task = new MutableLiveData<>();
    private final MutableLiveData<OperationResponseModel> taskIsCreated = new MutableLiveData<>();
    private final MutableLiveData<OperationResponseModel> taskIsDeleted = new MutableLiveData<>();
    private final MutableLiveData<OperationResponseModel> taskIsUpdated = new MutableLiveData<>();
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

    public void refreshBypassCache(){

        getAllTasksFromRemote();
    }


    public MutableLiveData<List<Task>> getMyTasks(){
        Long updateTime = preferencesHelper.getUpdateTime();
        Long currentTime = System.nanoTime();

        if(!(updateTime != 0 && currentTime -updateTime < GlobalConstants.REFRESH_TIME)){
            getAllTasksFromRemote();
        }
        return getMyTasksFromLocal();
    }


    private MutableLiveData<List<Task>> getAllTasksFromLocal(){
        taskRunner.executeAsync(new GetAllTasksFromLocalTask(taskDao), this::tasksRetrieved);
        return allTasks;
    }

    public MutableLiveData<List<Task>> getMyTasksFromLocal(){

        LoggedInUser user = preferencesHelper.getUser();
        taskRunner.executeAsync(new GetTaskByUserIdTask(taskDao, user.userId), this::myTasksRetrieved);
        return myTasks;
    }

    public void getMyTasksFromLocalByStatus(int status){
        LoggedInUser user = preferencesHelper.getUser();
        taskRunner.executeAsync(new GetTaskByUserIdAndStatusTask(taskDao, user.userId, status), this::myTasksRetrieved);
    }


    public MutableLiveData<List<Task>> getTasksByTeamId(String teamId){
        taskRunner.executeAsync(new GetTaskByTeamIdTask(taskDao, teamId), this::teamTasksRetrieved);
        return teamTasks;
    }

    public void getTasksByTeamIdAndStatus(String teamId, int status){
        taskRunner.executeAsync(new GetTaskByTeamIdAndStatusTask(taskDao, teamId, status), this::teamTasksRetrieved);
    }




    public MutableLiveData<Task> getTask(String taskId){
        taskRunner.executeAsync(new GetTaskByIdTask(taskDao, taskId), this::taskRetrieved);
        return task;
    }

    public void updateTask(Task task){
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
                                setIsUpdatedLiveData(OperationResponseModel.successfulResponse("Update"));
                            }
                            @SneakyThrows
                            @Override
                            public void onError(@NonNull Throwable e) {
                                setIsUpdatedLiveData(OperationResponseModel.failedResponse("Update",e));
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
                                setIsCreatedLiveData(OperationResponseModel.successfulResponse("Add"));
                            }
                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsCreatedLiveData(OperationResponseModel.failedResponse("Add",e));
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
                                setIsDeletedLiveData(operationResponseModel);
                                taskRunner.executeAsync(new DeleteTaskInLocalTask(taskDao, task), (data) ->{});
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsDeletedLiveData(OperationResponseModel.failedResponse("Delete",e));
                                e.printStackTrace();
                            }
                        })
        );
    }


    public MutableLiveData<List<Task>> getAllTasksFromRemote() {
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTasksApi().getAllTasks(user.token, 0, 1000)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Task>>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull List<Task> taskList) {
                                taskRunner.executeAsync(new TaskRepository.InsertTasksFromRemoteToLocalTask(taskDao, taskList), (data) -> {
                                    allTasksRetrieved(data);
                                    getMyTasksFromLocal();
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


    public void allTasksRetrieved(List<Task> tasksList){
        allTasks.setValue(tasksList);
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

    public void setIsCreatedLiveData(OperationResponseModel value){
        taskIsCreated.setValue(value);
    }

    public MutableLiveData<OperationResponseModel> getIsCreatedLiveData(){
        return taskIsCreated;
    }

    public void setIsUpdatedLiveData(OperationResponseModel value){
        taskIsUpdated.setValue(value);
    }

    public MutableLiveData<OperationResponseModel> getIsUpdatedLiveData(){
        return taskIsUpdated;
    }

    public void setIsDeletedLiveData(OperationResponseModel value){
        taskIsDeleted.setValue(value);
    }

    public MutableLiveData<OperationResponseModel> getIsDeletedLiveData(){
        return taskIsDeleted;
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
