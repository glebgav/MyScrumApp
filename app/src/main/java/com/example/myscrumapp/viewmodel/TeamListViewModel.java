package com.example.myscrumapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.repository.TaskRepository;
import com.example.myscrumapp.model.repository.TeamRepository;

import java.util.List;

import lombok.Getter;

@Getter
public class TeamListViewModel extends AndroidViewModel {

    private MutableLiveData<List<Team>> teams;
    private MutableLiveData<Boolean> teamLoadError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private TeamRepository teamRepository;

    public TeamListViewModel(@NonNull Application application) {
        super(application);
        teamRepository = new TeamRepository(application);
        teams = teamRepository.getAllTeams();

    }
    public  MutableLiveData<Boolean> getTeamLoadError(){
        return teamLoadError;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<List<Team>> getTeamsLiveData() {
        return teams;
    }


    public void refreshBypassCache(){
        teamRepository.refreshBypassCache();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }

}
