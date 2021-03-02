package com.example.myscrumapp.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myscrumapp.R;
import com.example.myscrumapp.view.adapter.TaskListAdapter;
import com.example.myscrumapp.viewmodel.ListViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ListFragment extends Fragment {
    private ListViewModel viewModel;
    private TaskListAdapter taskListAdapter = new TaskListAdapter(new ArrayList<>());

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

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(ListViewModel.class);

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
        viewModel.getTasksLiveData().observe(getViewLifecycleOwner(), tasks -> {
            if(tasks instanceof List){
                viewModel.getIsLoading().postValue(false);
                viewModel.getTaskLoadError().postValue(false);
                tasksList.setVisibility(View.VISIBLE);
                taskListAdapter.updateTasksList(tasks);
            }else
            {
                viewModel.getTaskLoadError().postValue(true);
            }
        });

        viewModel.getTaskLoadError().observe(getViewLifecycleOwner(), isError -> {
            if(isError instanceof Boolean){
                listError.setVisibility(isError?View.VISIBLE: View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(isLoading instanceof Boolean){
                loadingView.setVisibility(isLoading?View.VISIBLE: View.GONE);
                if(isLoading) {
                    listError.setVisibility(View.GONE);
                    tasksList.setVisibility(View.GONE);
                }
            }
        });
    }


}