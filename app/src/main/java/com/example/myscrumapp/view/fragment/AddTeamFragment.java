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
import com.example.myscrumapp.databinding.FragmentAddTeamBinding;
import com.example.myscrumapp.model.entity.Item;
import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.TeamToCreate;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.example.myscrumapp.viewmodel.AddTeamViewModel;
import com.google.android.material.snackbar.Snackbar;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.ArrayList;
import java.util.Objects;

public class AddTeamFragment extends Fragment {
    private FragmentAddTeamBinding binding;
    private AddTeamViewModel viewModel;
    private final SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(getContext());

    public AddTeamFragment() {
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_team, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(AddTeamViewModel.class);
        configureListeners();

        observeViewModel(view);
    }

    private void configureListeners(){
        binding.saveTeam.setOnClickListener(v -> {
            TeamToCreate teamToCreate = checkInputAndCreateTeam(binding);
            if(teamToCreate != null){
                viewModel.setIsLoading(true);
                viewModel.addTeam(teamToCreate);
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
            binding.usersInTeamListSpinner.setItems(items);
        });

        viewModel.getTasksLiveData().observe(getViewLifecycleOwner(), tasks -> {
            ArrayList<Item> items = new ArrayList<>();
            if(tasks != null){
                for(Task task: tasks){
                    items.add(Item.builder().name(task.getTitle()).value(false).obj(task).build());
                }
            }
            binding.tasksInTeamListSpinner.setItems(items);


        });
        viewModel.getIsTeamCreated().observe(getViewLifecycleOwner(), created -> {
            if(created != null){
                binding.teamAddedLoadingView.setVisibility(View.GONE);
                binding.editTextTeamName.setVisibility(View.VISIBLE);
                binding.saveTeam.setVisibility(View.VISIBLE);
                binding.tasksInTeamListSpinnerLayout.setVisibility(View.VISIBLE);
                binding.usersInTeamListSpinnerLayout.setVisibility(View.VISIBLE);
                if(created){
                    Snackbar.make(view, "Team Created Successfully", Snackbar.LENGTH_LONG).show();
                }
                if(!created){
                    Snackbar.make(view, "Error Creating the Team", Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(isLoading != null){
                binding.teamAddedLoadingView.setVisibility(isLoading?View.VISIBLE: View.GONE);
                if(isLoading) {
                    binding.editTextTeamName.setVisibility(View.GONE);
                    binding.saveTeam.setVisibility(View.GONE);
                    binding.tasksInTeamListSpinnerLayout.setVisibility(View.GONE);
                    binding.usersInTeamListSpinnerLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    private  TeamToCreate checkInputAndCreateTeam(FragmentAddTeamBinding binding){
        boolean validInput = true;

        String teamName = Objects.requireNonNull(binding.editTextTeamName.getText()).toString().trim();


        if(teamName.isEmpty()){
            binding.editTextTeamName.setError("Enter a valid team name");
            binding.editTextTeamName.requestFocus();
            validInput = false;
        }

        if(validInput){
            ArrayList<User> users = new ArrayList<>();
            for(Item item:  binding.usersInTeamListSpinner.getSelectedItems())
                users.add((User) item.getObj());

            ArrayList<Task> tasks = new ArrayList<>();
            for(Item item:  binding.tasksInTeamListSpinner.getSelectedItems())
                tasks.add((Task) item.getObj());

            return new TeamToCreate(teamName, tasks,users);
        }else{
            return null;
        }

    }
}