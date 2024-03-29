package com.example.myscrumapp.model.repository;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.network.OperationResponseModel;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Repository for user entities (from REST service)
 */
public class UserRepository {
    private final ApiService apiService;
    private final MutableLiveData<OperationResponseModel> userIsCreated = new MutableLiveData<>();
    private final MutableLiveData<OperationResponseModel> userIsUpdated = new MutableLiveData<>();
    private final MutableLiveData<OperationResponseModel> userIsDeleted = new MutableLiveData<>();
    private final MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private final MutableLiveData<List<UserRegisterDetails>> allUsersWithTeamsAndTasks = new MutableLiveData<>();
    private final SharedPreferencesHelper preferencesHelper;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public UserRepository(Application application) {
        preferencesHelper = SharedPreferencesHelper.getInstance(application);
        apiService = ApiService.getInstance();
    }

    /**
     * add user to remote
     * @param user user to add
     */
    public void addUser(UserRegisterDetails user){
        disposable.add(
                apiService.getUsersApi().createUser(user)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<UserRegisterDetails>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull UserRegisterDetails createdUser) {
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

    /**
     * delete user from remote
     * @param userToDelete user to delete
     */
    public void deleteUser(UserRegisterDetails userToDelete){
        LoggedInUser myUser = preferencesHelper.getUser();
        disposable.add(
                apiService.getUsersApi().deleteUser(myUser.token, userToDelete.userId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<OperationResponseModel>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull OperationResponseModel operationResponseModel) {
                                setIsDeletedLiveData(operationResponseModel);
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsDeletedLiveData(OperationResponseModel.failedResponse("Delete",e));
                                e.printStackTrace();
                            }
                        })
        );
    }

    /**
     * update user from remote
     * @param userToUpdate user to update
     */
    public void updateUser(UserRegisterDetails userToUpdate){
        LoggedInUser myUser = preferencesHelper.getUser();
        disposable.add(
                apiService.getUsersApi().updateUser(myUser.token, userToUpdate.userId, userToUpdate)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<UserRegisterDetails>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull UserRegisterDetails updatedUser) {
                                setIsUpdatedLiveData(OperationResponseModel.successfulResponse("Update"));
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsUpdatedLiveData(OperationResponseModel.failedResponse("Update",e));
                                e.printStackTrace();
                            }
                        })
        );
    }



    public MutableLiveData<List<User>> getAllUsers() {
        return fetchAllFromRemote();
    }

    /**
     * get all details of user  from remote
     * @return users with teams and tasks information
     */
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

    public void setIsCreatedLiveData(OperationResponseModel value) {
        userIsCreated.setValue(value);
    }

    public MutableLiveData<OperationResponseModel> getIsCreatedLiveData() {
        return userIsCreated;
    }

    public void setIsUpdatedLiveData(OperationResponseModel value) {
        userIsUpdated.setValue(value);
    }

    public MutableLiveData<OperationResponseModel> getIsUpdatedLiveData() {
        return userIsUpdated;
    }

    public void setIsDeletedLiveData(OperationResponseModel value) {
        userIsDeleted.setValue(value);
    }

    public MutableLiveData<OperationResponseModel> getIsDeletedLiveData() {
        return userIsDeleted;
    }



}
