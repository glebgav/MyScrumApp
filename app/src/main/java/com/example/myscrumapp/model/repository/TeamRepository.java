package com.example.myscrumapp.model.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.TeamToCreate;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.room.dao.TeamDao;
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
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamRepository {

    private final TeamDao teamDao;
    private final MutableLiveData<List<Team>> allTeams  = new MutableLiveData<>();
    private final MutableLiveData<Team> team = new MutableLiveData<>();
    private final MutableLiveData<Boolean> teamIsCreated = new MutableLiveData<>();
    private final ApiService apiService;
    private final TaskRunner taskRunner = new TaskRunner();
    private final SharedPreferencesHelper preferencesHelper;
    private final CompositeDisposable disposable = new CompositeDisposable();


    public TeamRepository(Application application){
        MyDatabase database = MyDatabase.getInstance(application);
        preferencesHelper = SharedPreferencesHelper.getInstance(application);
        teamDao = database.teamDao();
        apiService = ApiService.getInstance();
    }

    public void setIsCreatedLiveData(Boolean value){
        teamIsCreated.setValue(value);
    }

    public MutableLiveData<Boolean> getIsCreatedLiveData(){
        return teamIsCreated;
    }

    public MutableLiveData<List<Team>> getAllTeams(){
        Long updateTime = preferencesHelper.getUpdateTime();
        Long currentTime = System.nanoTime();
        if(updateTime != 0 && currentTime -updateTime < GlobalConstants.REFRESH_TIME) {
            return fetchFromDatabase();
        }else{
            return fetchFromRemote();
        }
    }

    public MutableLiveData<Team> getTeam(String teamId){
        taskRunner.executeAsync(new getTeamByTeamId(teamDao, teamId), this::teamRetrieved);
        return team;
    }

    public void addTeam(TeamToCreate team){
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTeamsApi().createTeam(user.token, team)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<TeamToCreate>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull TeamToCreate team) {
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

    public MutableLiveData<List<Team>> getAllTeamsFromRemote(){
        return fetchFromRemote();
    }

    public void teamsRetrieved(List<Team> teamsList){
        allTeams.setValue(teamsList);
    }

    public void teamRetrieved(Team team){
        this.team.setValue(team);
    }


    public void refreshBypassCache(){
        fetchFromRemote();
    }

    private MutableLiveData<List<Team>> fetchFromDatabase(){
        taskRunner.executeAsync(new getAllTeamsFromLocalTask(teamDao), this::teamsRetrieved);
        return allTeams;
    }

    private MutableLiveData<List<Team>> fetchFromRemote() {
        LoggedInUser user = preferencesHelper.getUser();
                disposable.add(
                apiService.getTeamsApi().getTeamsByUserId(user.token, user.userId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Team>>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull List<Team> teamsList) {
                                taskRunner.executeAsync(new InsertTeamsByUserIdFromRemoteToLocalTask(teamDao, teamsList), (data) ->{
                                    teamsRetrieved(data);
                                    preferencesHelper.saveUpdateTime(System.nanoTime());
                                });
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                e.printStackTrace();
                            }
                        })
        );
        return allTeams;

    }

    private static class InsertTeamsByUserIdFromRemoteToLocalTask implements Callable<List<Team>> {

        private final List<Team>[] lists;
        private final TeamDao teamDao;

        @SafeVarargs
        private InsertTeamsByUserIdFromRemoteToLocalTask(TeamDao teamDao, List<Team>... lists) {
            this.teamDao = teamDao;
            this.lists = lists;
        }

        @Override
        public List<Team> call() {
            List<Team> list = lists[0];
            teamDao.deleteAllTeams();

            ArrayList<Team> newList = new ArrayList<>(list);
            List<Long> result = teamDao.insertAll(newList.toArray(new Team[0]));

            int i=0;
            while (i<list.size()) {
                list.get(i).setId(result.get(i).intValue());
                ++i;
            }

            return list;
        }
    }

    private static class getAllTeamsFromLocalTask implements Callable<List<Team>>{
        private final TeamDao teamDao;
        public getAllTeamsFromLocalTask(TeamDao teamDao){
            this.teamDao = teamDao;
        }

        @Override
        public List<Team> call() {
            return teamDao.getAllTeams();
        }
    }

    private static class getTeamByTeamId implements Callable<Team> {
        private final String teamId;
        private final TeamDao teamDao;

        public getTeamByTeamId(TeamDao teamDao, String teamId)
        {
            this.teamDao = teamDao;
            this.teamId = teamId;
        }

        @Override
        public Team call() {
            return teamDao.getTeamByTeamId(teamId);
        }
    }





}
