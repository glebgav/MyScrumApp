package com.example.myscrumapp.model.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.network.ApiService;
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
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final ApiService apiService;
    private final TaskDao taskDao;
    private final MutableLiveData<Boolean> userIsCreated = new MutableLiveData<>();
    private final MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private final TaskRunner taskRunner = new TaskRunner();
    private final SharedPreferencesHelper preferencesHelper;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public UserRepository(Application application) {
        MyDatabase database = MyDatabase.getInstance(application);
        preferencesHelper = SharedPreferencesHelper.getInstance(application);
        apiService = ApiService.getInstance();
        taskDao = database.taskDao();
    }

    public void setIsCreatedLiveData(Boolean value) {
        userIsCreated.setValue(value);
    }

    public MutableLiveData<Boolean> getIsCreatedLiveData() {
        return userIsCreated;
    }

    public void addUser(UserRegisterDetails user) {
        Call<UserRegisterDetails> call = apiService
                .getUsersApi()
                .createUser(user);

        call.enqueue(new Callback<UserRegisterDetails>() {
            @SneakyThrows
            @Override
            public void onResponse(@NonNull Call<UserRegisterDetails> call, @NonNull Response<UserRegisterDetails> response) {
                setIsCreatedLiveData(response.code() == 200);
            }

            @Override
            public void onFailure(@NonNull Call<UserRegisterDetails> call, @NonNull Throwable t) {
                setIsCreatedLiveData(false);
            }
        });
    }

/*    public MutableLiveData<List<User>> getAllUsers(){
        taskRunner.executeAsync(new UserRepository.GetAllUsersFromLocalTask(taskDao), this::usersRetrieved);
        return allUsers;
    }*/

    public MutableLiveData<List<User>> getAllUsers() {
        return fetchAllFromRemote();
    }

    private void usersRetrieved(List<User> users) {
        allUsers.setValue(users);
    }

    private MutableLiveData<List<User>> fetchAllFromRemote() {
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getUsersApi().getAllUsers(user.token, 0, 50)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<User>>() {

                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull List<User> users) {
                                usersRetrieved(users);
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                e.printStackTrace();
                            }
                        })
        );
        return allUsers;

    }


    private static class GetAllUsersFromLocalTask implements Callable<List<User>> {
        private final TaskDao taskDao;

        public GetAllUsersFromLocalTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        public List<User> call() {
            List<User> users = new ArrayList<>();
            for (Task task : taskDao.getAllTasks()) {
                users.add(task.getUserDetails());
            }
            return users;
        }
    }


}
