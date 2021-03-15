package com.example.myscrumapp.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProviders;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.myscrumapp.R;
import com.example.myscrumapp.databinding.FragmentAddUserBinding;
import com.example.myscrumapp.model.entity.Item;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.view.shared.MultiSelectionSpinner;
import com.example.myscrumapp.viewmodel.AddUserViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;



public class AddUserFragment extends Fragment {
    private AddUserViewModel viewModel;
    private FragmentAddUserBinding binding;


    public AddUserFragment() {
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_user, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(AddUserViewModel.class);
        configureListeners();

        observeViewModel(view);

    }

    private void configureListeners(){
        binding.saveUser.setOnClickListener(v -> {
            UserRegisterDetails userToCreate = checkInputAndCreateUser(binding);
            if(userToCreate != null){
                viewModel.setIsLoading(true);
                viewModel.addUser(userToCreate);
            }

        });
    }

    private void observeViewModel(View view){
        viewModel.getTeamsLiveData().observe(getViewLifecycleOwner(), teams -> {
            ArrayList<Item> items = new ArrayList<>();
            if(teams != null){
                for(Team team: teams){
                    items.add(Item.builder().name(team.getName()).value(false).obj(team).build());
                }
            }
            binding.teamListSpinner.setItems(items);
        });

        viewModel.getTasksLiveData().observe(getViewLifecycleOwner(), tasks -> {
            ArrayList<Item> items = new ArrayList<>();
            if(tasks != null){
                for(Task task: tasks){
                    items.add(Item.builder().name(task.getTitle()).value(false).obj(task).build());
                }
            }
            binding.taskListSpinner.setItems(items);


        });
        viewModel.getIsUserCreated().observe(getViewLifecycleOwner(), created -> {
            if(created != null){
                binding.userAddedLoadingView.setVisibility(View.GONE);
                binding.editTextPassword.setVisibility(View.VISIBLE);
                binding.editTextFirstName.setVisibility(View.VISIBLE);
                binding.editTextLastName.setVisibility(View.VISIBLE);
                binding.editTextEmail.setVisibility(View.VISIBLE);
                binding.isManager.setVisibility(View.VISIBLE);
                binding.saveUser.setVisibility(View.VISIBLE);
                binding.teamListSpinnerLayout.setVisibility(View.VISIBLE);
                binding.taskListSpinnerLayout.setVisibility(View.VISIBLE);
                if(created){
                    Snackbar.make(view, "User Created Successfully", Snackbar.LENGTH_LONG).show();
                }
                if(!created){
                    Snackbar.make(view, "Error Creating the User", Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(isLoading != null){
                binding.userAddedLoadingView.setVisibility(isLoading?View.VISIBLE: View.GONE);
                if(isLoading) {
                    binding.editTextPassword.setVisibility(View.GONE);
                    binding.editTextFirstName.setVisibility(View.GONE);
                    binding.editTextLastName.setVisibility(View.GONE);
                    binding.editTextEmail.setVisibility(View.GONE);
                    binding.isManager.setVisibility(View.GONE);
                    binding.saveUser.setVisibility(View.GONE);
                    binding.teamListSpinnerLayout.setVisibility(View.GONE);
                    binding.taskListSpinnerLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    private  UserRegisterDetails checkInputAndCreateUser(FragmentAddUserBinding binding){
        boolean validInput = true;

        String email = Objects.requireNonNull(binding.editTextEmail.getText()).toString().trim();
        String firstName = Objects.requireNonNull(binding.editTextFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTextLastName.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTextPassword.getText()).toString().trim();
        Boolean isManager = binding.isManager.isChecked();

        if(email.isEmpty()){
            binding.editTextEmail.setError("Enter a valid email");
            binding.editTextEmail.requestFocus();
            validInput = false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.editTextEmail.setError("Enter a valid email");
            binding.editTextEmail.requestFocus();
            validInput = false;
        }
        if(password.isEmpty()){
            binding.editTextPassword.setError("Password required");
            binding.editTextPassword.requestFocus();
            validInput = false;
        }
        if(password.length() < 6){
            binding.editTextPassword.setError("Password should be at least 6 characters long");
            binding.editTextPassword.requestFocus();
            validInput = false;
        }
        if(firstName.isEmpty()){
            binding.editTextFirstName.setError("First name required");
            binding.editTextFirstName.requestFocus();
            validInput = false;
        }
        if(lastName.isEmpty()){
            binding.editTextLastName.setError("Last name required");
            binding.editTextLastName.requestFocus();
            validInput = false;
        }


        if(validInput){
            ArrayList<Team> teams = new ArrayList<>();
            for(Item item:  binding.teamListSpinner.getSelectedItems())
                teams.add((Team) item.getObj());

            ArrayList<Task> tasks = new ArrayList<>();
            for(Item item:  binding.taskListSpinner.getSelectedItems())
                tasks.add((Task) item.getObj());

            return new UserRegisterDetails(firstName, lastName,password,email, isManager, teams,tasks);
        }else{
            return null;
        }

    }
}