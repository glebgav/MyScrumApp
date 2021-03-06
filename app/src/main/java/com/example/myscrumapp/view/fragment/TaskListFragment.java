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
import com.example.myscrumapp.utils.GlobalConstants;
import com.example.myscrumapp.view.adapter.TaskListAdapter;
import com.example.myscrumapp.viewmodel.TaskListViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TaskListFragment extends Fragment {
    private String teamId;
    private TaskListViewModel viewModel;
    private final TaskListAdapter taskListAdapter = new TaskListAdapter(new ArrayList<>());

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tasksList)
    RecyclerView tasksList;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.listError)
    TextView listError;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.loadingView)
    ProgressBar loadingView;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;

    public TaskListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(TaskListViewModel.class);

        if(getArguments() != null){
            teamId = TaskListFragmentArgs.fromBundle(getArguments()).getTeamId();
        }
        if(!teamId.equals(GlobalConstants.MY_TASKS_FRAGMENT_INDICATOR)){
            viewModel.setTeamIdLiveData(teamId);
        }else{
            viewModel.setToMyTasks();
        }


        tasksList.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksList.setAdapter(taskListAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            tasksList.setVisibility(View.GONE);
            listError.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            viewModel.refreshBypassCache();
            refreshLayout.setRefreshing(false);
        });



        observeViewModel();

    }

    private void observeViewModel(){
        viewModel.getMyTasksLiveData().observe(getViewLifecycleOwner(), tasks -> {
            if(tasks != null){
                viewModel.getIsLoading().postValue(false);
                viewModel.getTaskLoadError().postValue(false);
                tasksList.setVisibility(View.VISIBLE);
                if(teamId.equals(GlobalConstants.MY_TASKS_FRAGMENT_INDICATOR))
                    taskListAdapter.updateTasksList(tasks);
            }else
            {
                viewModel.getTaskLoadError().postValue(true);
            }
        });
        viewModel.getTasksLiveData().observe(getViewLifecycleOwner(), tasks -> {
            if(tasks != null){
                viewModel.getIsLoading().postValue(false);
                viewModel.getTaskLoadError().postValue(false);
                tasksList.setVisibility(View.VISIBLE);
                if(!teamId.equals(GlobalConstants.MY_TASKS_FRAGMENT_INDICATOR)) {
                    viewModel.setTeamIdLiveData(teamId);
                    taskListAdapter.updateTasksList(tasks);
                }

            }else
            {
                viewModel.getTaskLoadError().postValue(true);
            }
        });

        viewModel.getTaskLoadError().observe(getViewLifecycleOwner(), isError -> {
            if(isError != null){
                listError.setVisibility(isError?View.VISIBLE: View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(isLoading != null){
                loadingView.setVisibility(isLoading?View.VISIBLE: View.GONE);
                if(isLoading) {
                    listError.setVisibility(View.GONE);
                    tasksList.setVisibility(View.GONE);
                }
            }
        });
    }


}