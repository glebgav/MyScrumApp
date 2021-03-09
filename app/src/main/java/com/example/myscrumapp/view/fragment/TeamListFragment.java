package com.example.myscrumapp.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.myscrumapp.R;
import com.example.myscrumapp.view.adapter.TeamListAdapter;
import com.example.myscrumapp.viewmodel.TeamListViewModel;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;


public class TeamListFragment extends Fragment {
    private TeamListViewModel viewModel;
    private final TeamListAdapter teamListAdapter = new TeamListAdapter(new ArrayList<>());

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.teamsList)
    RecyclerView teamsList;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.teamListError)
    TextView listError;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.teamLoadingView)
    ProgressBar loadingView;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.teamRefreshLayout)
    SwipeRefreshLayout refreshLayout;

    public TeamListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_team_list, container, false);
        ButterKnife.bind(this,view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(TeamListViewModel.class);

        teamsList.setLayoutManager(new LinearLayoutManager(getContext()));
        teamsList.setAdapter(teamListAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            teamsList.setVisibility(View.GONE);
            listError.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            viewModel.refreshBypassCache();
            refreshLayout.setRefreshing(false);
        });

        observeViewModel();

    }

    private void observeViewModel() {
        viewModel.getTeamsLiveData().observe(getViewLifecycleOwner(), teams -> {
            if(teams != null){
                viewModel.getIsLoading().postValue(false);
                viewModel.getTeamLoadError().postValue(false);
                teamsList.setVisibility(View.VISIBLE);
                teamListAdapter.updateTeamsList(teams);
            }else
            {
                viewModel.getTeamLoadError().postValue(true);
            }
        });
        viewModel.getTeamLoadError().observe(getViewLifecycleOwner(), isError -> {
            if(isError != null){
                listError.setVisibility(isError?View.VISIBLE: View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(isLoading != null){
                loadingView.setVisibility(isLoading?View.VISIBLE: View.GONE);
                if(isLoading) {
                    listError.setVisibility(View.GONE);
                    teamsList.setVisibility(View.GONE);
                }
            }
        });
    }
}