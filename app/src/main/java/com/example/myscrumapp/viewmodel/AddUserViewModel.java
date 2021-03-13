package com.example.myscrumapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.repository.UserRepository;

public class AddUserViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> userCreated;
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public AddUserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        userCreated = userRepository.getIsCreatedLiveData();
    }

    public void addUser(UserRegisterDetails user){
        userRepository.addUser(user);
    }

    public  MutableLiveData<Boolean> getIsUserCreated(){
        return userCreated;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setIsLoading(boolean value) {
        isLoading.setValue(value);
    }



}
