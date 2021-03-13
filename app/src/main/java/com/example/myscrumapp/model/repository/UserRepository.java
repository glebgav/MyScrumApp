package com.example.myscrumapp.model.repository;

import android.app.Application;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.room.db.MyDatabase;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.example.myscrumapp.utils.TaskRunner;
import com.example.myscrumapp.view.activity.SignUpActivity;

import java.util.List;

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
    private final TaskRunner taskRunner = new TaskRunner();
    private final SharedPreferencesHelper preferencesHelper;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public UserRepository(Application application){
        preferencesHelper = SharedPreferencesHelper.getInstance(application);
        apiService = ApiService.getInstance();
    }

    public void setIsCreatedLiveData(Boolean value){
        userIsCreated.setValue(value);
    }

    public MutableLiveData<Boolean> getIsCreatedLiveData(){
        return userIsCreated;
    }

    public void addUser(UserRegisterDetails user){
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


}
