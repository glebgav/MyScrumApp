package com.example.myscrumapp.model.repository;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.network.OperationResponseModel;
import com.example.myscrumapp.model.room.dao.TeamDao;
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

public class TeamRepository {

    private final TeamDao teamDao;
    private final ApiService apiService;

    private final MutableLiveData<List<Team>> myTeams = new MutableLiveData<>();
    private final MutableLiveData<List<Team>> allTeams = new MutableLiveData<>();
    private final MutableLiveData<Team> team = new MutableLiveData<>();
    private final MutableLiveData<OperationResponseModel> teamIsCreated = new MutableLiveData<>();
    private final MutableLiveData<OperationResponseModel> teamIsUpdated = new MutableLiveData<>();
    private final MutableLiveData<OperationResponseModel> teamIsDeleted = new MutableLiveData<>();

    private final TaskRunner taskRunner = new TaskRunner();
    private final SharedPreferencesHelper preferencesHelper;
    private final CompositeDisposable disposable = new CompositeDisposable();


    public TeamRepository(Application application) {
        MyDatabase database = MyDatabase.getInstance(application);
        preferencesHelper = SharedPreferencesHelper.getInstance(application);
        teamDao = database.teamDao();
        apiService = ApiService.getInstance();
    }

    public void refreshBypassCache() {
        getAllTeamsFromRemote();
    }


    public MutableLiveData<List<Team>> getMyTeams() {
        Long updateTime = preferencesHelper.getUpdateTime();
        Long currentTime = System.nanoTime();
        if (updateTime != 0 && currentTime - updateTime < GlobalConstants.REFRESH_TIME) {
            getAllTeamsFromRemote();
        } else {
            getMyTeamsFromLocal();
        }
        return myTeams;
    }

    public MutableLiveData<List<Team>> getAllTeams() {
        getAllTeamsFromRemote();
        return allTeams;
    }

    public void getMyTeamsFromLocal() {
        taskRunner.executeAsync(new GetMyTeamsFromLocalTask(teamDao), this::myTeamsRetrieved);
    }


    public MutableLiveData<Team> getTeam(String teamId) {
        taskRunner.executeAsync(new GetTeamByTeamId(teamDao, teamId), this::teamRetrieved);
        return team;
    }

    public void addTeamInRemote(Team team) {
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTeamsApi().createTeam(user.token, team)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Team>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull Team createdTeam) {
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

    public void updateTeam(Team team) {
        taskRunner.executeAsync(new TeamRepository.UpdateTeamInLocalTask(teamDao, team), result -> updateTeamInRemote(team));
    }


    public void updateTeamInRemote(Team team) {
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTeamsApi().updateTeam(user.token, team.getTeamId(), team)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Team>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull Team teamFromRemote) {
                                setIsUpdatedLiveData(OperationResponseModel.successfulResponse("Update"));
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                setIsCreatedLiveData(OperationResponseModel.failedResponse("Update",e));
                                e.printStackTrace();
                            }
                        })
        );
    }

    public void deleteTeam(Team team) {
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTeamsApi().deleteTeam(user.token, team.getTeamId())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<OperationResponseModel>() {
                            @Override
                            public void onSuccess(@NonNull OperationResponseModel operationResponseModel) {
                                setIsDeletedLiveData(operationResponseModel);
                                taskRunner.executeAsync(new TeamRepository.DeleteTeamInLocalTask(teamDao, team), (data) -> {
                                });
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                setIsDeletedLiveData(OperationResponseModel.failedResponse("Delete",e));
                                e.printStackTrace();
                            }
                        })
        );
    }



    public MutableLiveData<List<Team>> getAllTeamsFromRemote() {
        LoggedInUser user = preferencesHelper.getUser();
        disposable.add(
                apiService.getTeamsApi().getAllTeams(user.token, 0, 50)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Team>>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull List<Team> teamsList) {
                                taskRunner.executeAsync(new InsertAllTeamsFromRemoteToLocalTask(teamDao, user.userId, teamsList), (data) -> {
                                    allTeamsRetrieved(data);
                                    getMyTeamsFromLocal();
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


    public MutableLiveData<OperationResponseModel> getIsCreatedLiveData() {
        return teamIsCreated;
    }

    public void setIsCreatedLiveData(OperationResponseModel value) {
        teamIsCreated.setValue(value);
    }

    public MutableLiveData<OperationResponseModel> getIsUpdatedLiveData() {
        return teamIsUpdated;
    }

    public void setIsUpdatedLiveData(OperationResponseModel value) {
        teamIsUpdated.setValue(value);
    }

    public MutableLiveData<OperationResponseModel> getIsDeletedLiveData() {
        return teamIsDeleted;
    }

    public void setIsDeletedLiveData(OperationResponseModel value) {
        teamIsDeleted.setValue(value);
    }

    public void myTeamsRetrieved(List<Team> teamsList) {
        myTeams.setValue(teamsList);
    }

    public void allTeamsRetrieved(List<Team> teamsList) {
        allTeams.setValue(teamsList);
    }

    public void teamRetrieved(Team team) {
        this.team.setValue(team);
    }


    private static class InsertAllTeamsFromRemoteToLocalTask implements Callable<List<Team>> {

        private final List<Team>[] lists;
        private final TeamDao teamDao;
        private final String userId;

        @SafeVarargs
        private InsertAllTeamsFromRemoteToLocalTask(TeamDao teamDao, String userId, List<Team>... lists) {
            this.teamDao = teamDao;
            this.lists = lists;
            this.userId = userId;
        }

        @Override
        public List<Team> call() {
            List<Team> list = lists[0];
            teamDao.deleteAllTeams();


            ArrayList<Team> newList = new ArrayList<>(list);


            for (Team team : newList) {
                team.setMyTeam(false);
                if (team.getUsers() != null) {
                    for (User user : team.getUsers()) {
                        if (user.userId.equals(this.userId)) {
                            team.setMyTeam(true);
                            break;
                        }
                    }
                }
            }
            List<Long> result = teamDao.insertAll(newList.toArray(new Team[0]));

            int i = 0;
            while (i < list.size()) {
                list.get(i).setId(result.get(i).intValue());
                ++i;
            }

            return list;
        }
    }

    private static class GetAllTeamsFromLocalTask implements Callable<List<Team>> {
        private final TeamDao teamDao;

        public GetAllTeamsFromLocalTask(TeamDao teamDao) {
            this.teamDao = teamDao;
        }

        @Override
        public List<Team> call() {
            return teamDao.getAllTeams();
        }
    }



    private static class GetMyTeamsFromLocalTask implements Callable<List<Team>> {
        private final TeamDao teamDao;

        public GetMyTeamsFromLocalTask(TeamDao teamDao) {
            this.teamDao = teamDao;
        }

        @Override
        public List<Team> call() {
            return teamDao.getMyTeams();
        }
    }


    private static class GetTeamByTeamId implements Callable<Team> {
        private final String teamId;
        private final TeamDao teamDao;

        public GetTeamByTeamId(TeamDao teamDao, String teamId) {
            this.teamDao = teamDao;
            this.teamId = teamId;
        }

        @Override
        public Team call() {
            return teamDao.getTeamByTeamId(teamId);
        }
    }

    private static class UpdateTeamInLocalTask implements Callable<Void> {
        private final TeamDao teamDao;
        private final Team team;

        public UpdateTeamInLocalTask(TeamDao teamDao, Team team) {
            this.teamDao = teamDao;
            this.team = team;
        }

        @Override
        public Void call() {
            return teamDao.update(team);
        }
    }

    private static class DeleteTeamInLocalTask implements Callable<Void> {
        private final TeamDao teamDao;
        private final Team team;

        public DeleteTeamInLocalTask(TeamDao teamDao, Team team) {
            this.teamDao = teamDao;
            this.team = team;
        }

        @Override
        public Void call() {
            return teamDao.delete(team);
        }
    }


}
