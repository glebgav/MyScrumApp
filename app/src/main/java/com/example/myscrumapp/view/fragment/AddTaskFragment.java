package com.example.myscrumapp.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myscrumapp.R;
import com.example.myscrumapp.databinding.FragmentAddTaskBinding;
import com.example.myscrumapp.databinding.FragmentAddTeamBinding;
import com.example.myscrumapp.model.entity.Item;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.TeamInTask;
import com.example.myscrumapp.model.entity.TeamToCreate;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.example.myscrumapp.viewmodel.AddTaskViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;


public class AddTaskFragment extends Fragment {
    private FragmentAddTaskBinding binding;
    private AddTaskViewModel viewModel;
    private final SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(getContext());

    public AddTaskFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_task, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(AddTaskViewModel.class);

        ArrayList<Item> items = new ArrayList<>();
        items.add(Item.builder().name("To Do").value(false).obj(0).build());
        items.add(Item.builder().name("In Progress").value(false).obj(1).build());
        items.add(Item.builder().name("Done").value(false).obj(2).build());

        binding.statusSpinner.setItems(items);

        configureListeners();

        observeViewModel(view);
    }

    private void configureListeners(){
        binding.saveTask.setOnClickListener(v -> {
            Task taskToCreate = checkInputAndCreateTask(binding);
            if(taskToCreate != null){
                viewModel.setIsLoading(true);
                viewModel.addTask(taskToCreate);
            }

        });
    }

    private void observeViewModel(View view){
        viewModel.getUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            ArrayList<Item> items = new ArrayList<>();
            if(users != null){
                for(User user: users){
                    items.add(Item.builder().name(user.firstName + " " + user.lastName).value(false).obj(user).build());
                }
            }
            binding.userInTaskSpinner.setItems(items);
        });

        viewModel.getTeamsLiveData().observe(getViewLifecycleOwner(), teams -> {
            ArrayList<Item> items = new ArrayList<>();
            if(teams != null){
                for(Team team: teams){
                    items.add(Item.builder().name(team.getName()).value(false).obj(team).build());
                }
            }
            binding.teamInTaskSpinner.setItems(items);


        });
        viewModel.getIsTaskCreated().observe(getViewLifecycleOwner(), created -> {
            if(created != null){
                binding.taskAddedLoadingView.setVisibility(View.GONE);
                binding.editTextTitle.setVisibility(View.VISIBLE);
                binding.editTextDescription.setVisibility(View.VISIBLE);
                binding.saveTask.setVisibility(View.VISIBLE);
                binding.teamInTaskSpinnerLayout.setVisibility(View.VISIBLE);
                binding.userInTaskSpinnerLayout.setVisibility(View.VISIBLE);
                binding.statusSpinnerLayout.setVisibility(View.VISIBLE);
                if(created){
                    Snackbar.make(view, "Task Created Successfully", Snackbar.LENGTH_LONG).show();
                }
                if(!created){
                    Snackbar.make(view, "Error Creating the Task", Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(isLoading != null){
                binding.taskAddedLoadingView.setVisibility(isLoading?View.VISIBLE: View.GONE);
                if(isLoading) {
                    binding.editTextTitle.setVisibility(View.GONE);
                    binding.editTextDescription.setVisibility(View.GONE);
                    binding.saveTask.setVisibility(View.GONE);
                    binding.teamInTaskSpinnerLayout.setVisibility(View.GONE);
                    binding.userInTaskSpinnerLayout.setVisibility(View.GONE);
                    binding.statusSpinnerLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    private  Task checkInputAndCreateTask(FragmentAddTaskBinding binding){
        boolean validInput = true;
        String taskTitle = Objects.requireNonNull(binding.editTextTitle.getText()).toString().trim();
        String taskDescription = Objects.requireNonNull(binding.editTextDescription.getText()).toString().trim();


        if(taskTitle.isEmpty()){
            binding.editTextTitle.setError("Enter a valid task title");
            binding.editTextTitle.requestFocus();
            validInput = false;
        }

        if(taskDescription.isEmpty()){
            binding.editTextDescription.setError("Enter a valid task description");
            binding.editTextDescription.requestFocus();
            validInput = false;
        }

        if(binding.userInTaskSpinner.getSelectedItems().size() > 1){
            binding.userInTaskSpinner.requestFocus();
            validInput = false;
        }


        if(binding.teamInTaskSpinner.getSelectedItems().size() > 1){
            binding.teamInTaskSpinner.requestFocus();
            validInput = false;
        }


        if(validInput){

            int status = (int) binding.statusSpinner.getSelectedItems().get(0).getObj();

            User user = (User) binding.userInTaskSpinner.getSelectedItems().get(0).getObj();

            Team team = (Team) binding.teamInTaskSpinner.getSelectedItems().get(0).getObj();

            TeamInTask teamInTask = new TeamInTask();
            teamInTask.teamId = team.getTeamId();

            return new Task(null, taskTitle,taskDescription, status, user, teamInTask);
        }else{
            return null;
        }

    }
}