package com.example.myscrumapp.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.myscrumapp.R;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.utils.GlobalConstants;
import com.example.myscrumapp.utils.SecurityManager;
import com.example.myscrumapp.view.adapter.TaskListAdapter;
import com.example.myscrumapp.viewmodel.TaskListViewModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *  Fragment for task list - for a specific team or my tasks
 */
public class TaskListFragment extends Fragment {
    private String teamId;
    private TaskListViewModel viewModel;
    private final TaskListAdapter taskListAdapter = new TaskListAdapter(new ArrayList<>());
    private final SecurityManager securityManager = SecurityManager.getInstance(getContext());
    private AppBarLayout appBarLayout;
    private Button toDoBtn;
    private Button inProgressBtn;
    private Button doneBtn;

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
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout);
        toDoBtn = appBarLayout.findViewById(R.id.toDoButton);
        inProgressBtn = appBarLayout.findViewById(R.id.inProgressButton);
        doneBtn = appBarLayout.findViewById(R.id.doneButton);
        enableButtons();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(TaskListViewModel.class);

        tasksList.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksList.setAdapter(taskListAdapter);

        if(getArguments() != null){
            teamId = TaskListFragmentArgs.fromBundle(getArguments()).getTeamId();
        }
        if(!teamId.equals(GlobalConstants.MY_TASKS_FRAGMENT_INDICATOR)){
            viewModel.setTeamIdLiveData(teamId);
        }else{
            viewModel.setToMyTasks();
        }


        refreshLayout.setOnRefreshListener(() -> {
            tasksList.setVisibility(View.GONE);
            listError.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            viewModel.refreshBypassCache();
            refreshLayout.setRefreshing(false);
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Task task = taskListAdapter.getTaskAt(viewHolder.getAdapterPosition());
                if(securityManager.canModifyTask(task)){
                    if(direction == ItemTouchHelper.LEFT){
                        if(task.getStatus() > 0 )
                            task.setStatus(task.getStatus()-1);
                        viewModel.update(task);
                    }
                    else if(direction == ItemTouchHelper.RIGHT) {
                        if (task.getStatus() < 2)
                            task.setStatus(task.getStatus() + 1);
                        viewModel.update(task);
                    }
                }else{
                    Snackbar.make(view, "You can't change task status (no permissions)", Snackbar.LENGTH_LONG).show();
                }


            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(tasksList);


        configureListeners();

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
    private void enableButtons(){
        toDoBtn.setClickable(true);
        inProgressBtn.setClickable(true);
        doneBtn.setClickable(true);
    }

    private void disableButtons(){
        toDoBtn.setClickable(false);
        inProgressBtn.setClickable(false);
        doneBtn.setClickable(false);
    }

    private void filterByStatus(int status){
        if(teamId.equals(GlobalConstants.MY_TASKS_FRAGMENT_INDICATOR)){
            viewModel.filterMyTasksByStatus(status);
        }else{
            viewModel.filterTeamTasksByStatus(teamId, status);
        }
    }

    private void configureListeners(){
        toDoBtn.setOnClickListener(v -> filterByStatus(GlobalConstants.TODO_STATUS));

        inProgressBtn.setOnClickListener(v ->  filterByStatus(GlobalConstants.IN_PROGRESS_STATUS));

        doneBtn.setOnClickListener(v -> filterByStatus(GlobalConstants.DONE_STATUS));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disableButtons();


    }
}