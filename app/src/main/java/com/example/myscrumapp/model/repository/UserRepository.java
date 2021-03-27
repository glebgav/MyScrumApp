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
import com.example.myscrumapp.model.network.OperationResponseModel;
import com.example.myscrumapp.model.network.OperationResponseStatus;
import com.example.myscrumapp.model.network.UsersApi;
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
    private final MutableLiveData<Boolean> userIsCreated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userIsUpdated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userIsDeleted = new MutableLiveData<>();
    private final MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private final MutableLiveData<List<UserRegisterDetails>> allUsersWithTeamsAndTasks = new MutableLiveData<>();
    private final SharedPreferencesHelper preferencesHelper;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public UserRepository(Application application) {
        preferencesHelper = SharedPreferencesHelper.getInstance(application);
        apiService = ApiService.getInstance();
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

    public void deleteUser(UserRegisterDetails userToDelete){
        LoggedInUser myUser = preferencesHelper.getUser();
        disposable.add(
                apiService.getUsersApi().deleteUser(myUser.token, userToDelete.userId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<OperationResponseModel>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull OperationResponseModel operationResponseModel) {
                                setIsDeletedLiveData(operationResponseModel.getOperationResult().equals(OperationResponseStatus.SUCCESS.name()));
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsDeletedLiveData(false);
                                e.printStackTrace();
                            }
                        })
        );
    }


    public void updateUser(UserRegisterDetails userToUpdate){
        LoggedInUser myUser = preferencesHelper.getUser();
        disposable.add(
                apiService.getUsersApi().updateUser(myUser.token, userToUpdate.userId, userToUpdate)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<UserRegisterDetails>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull UserRegisterDetails updatedUser) {
                                setIsUpdatedLiveData(true);
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsUpdatedLiveData(false);
                                e.printStackTrace();
                            }
                        })
        );
    }



    public MutableLiveData<List<User>> getAllUsers() {
        return fetchAllFromRemote();
    }

    public MutableLiveData<List<UserRegisterDetails>> getAllUsersWithTeamsAndTasks() {
        return fetchAllFromRemoteWithTeamsAndTasks();
    }

    private void usersRetrieved(List<User> users) {
        allUsers.setValue(users);
    }

    private void usersWithTeamsAndTaskRetrieved(List<UserRegisterDetails> users) {
        allUsersWithTeamsAndTasks.setValue(users);
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

    private MutableLiveData<List<UserRegisterDetails>> fetchAllFromRemoteWithTeamsAndTasks() {
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getUsersApi().getAllUsersWithTeamsAndTasks(user.token, 0, 50)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<UserRegisterDetails>>() {

                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull List<UserRegisterDetails> users) {
                                usersWithTeamsAndTaskRetrieved(users);
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                e.printStackTrace();
                            }
                        })
        );
        return allUsersWithTeamsAndTasks;

    }

    public void setIsCreatedLiveData(Boolean value) {
        userIsCreated.setValue(value);
    }

    public MutableLiveData<Boolean> getIsCreatedLiveData() {
        return userIsCreated;
    }

    public void setIsUpdatedLiveData(Boolean value) {
        userIsUpdated.setValue(value);
    }

    public MutableLiveData<Boolean> getIsUpdatedLiveData() {
        return userIsUpdated;
    }

    public void setIsDeletedLiveData(Boolean value) {
        userIsDeleted.setValue(value);
    }

    public MutableLiveData<Boolean> getIsDeletedLiveData() {
        return userIsDeleted;
    }



}
