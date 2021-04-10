package com.example.myscrumapp.view.fragment;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.myscrumapp.R;
import com.example.myscrumapp.databinding.FragmentAddUserBinding;
import com.example.myscrumapp.model.entity.Item;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.network.OperationResponseStatus;
import com.example.myscrumapp.utils.GlobalConstants;
import com.example.myscrumapp.viewmodel.AddUserViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  Fragment for managing users as a Manager
 */
public class AddUserFragment extends Fragment {
    private AddUserViewModel viewModel;
    private FragmentAddUserBinding binding;
    private ArrayAdapter<Item> adapter;


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

        adapter = new ArrayAdapter<>(getContext(), R.layout.custom_spinner);
        adapter.add(Item.builder().name("").value(false).obj(null).build());

        binding.selectUserSpinner.setAdapter(adapter);

        configureListeners();

        observeViewModel(view);

    }

    private void configureListeners() {
        binding.saveUser.setOnClickListener(v -> {
            UserRegisterDetails userToCreate = checkInputAndCreateUser(binding, false);
            if (userToCreate != null) {
                viewModel.setIsLoading(true);
                viewModel.addUser(userToCreate);
            }

        });

        binding.editUser.setOnClickListener(v -> {
            UserRegisterDetails userToUpdate = checkInputAndCreateUser(binding, true);
            if (userToUpdate != null) {
                viewModel.setIsLoading(true);
                viewModel.updateUser(userToUpdate);
            }

        });

        binding.deleteUser.setOnClickListener(v -> {
            UserRegisterDetails userToDelete = checkInputForDelete(binding);
            if (userToDelete != null) {
                viewModel.setIsLoading(true);
                viewModel.deleteUser(userToDelete);
            }

        });

        binding.selectUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (binding.selectUserSpinner.getSelectedItem() != null) {
                    Item selectedItem = (Item) binding.selectUserSpinner.getSelectedItem();
                    if (selectedItem.getObj() instanceof UserRegisterDetails) {
                        binding.saveUser.setClickable(false);
                        UserRegisterDetails selectedUser = (UserRegisterDetails) selectedItem.getObj();
                        selectedUser.password = GlobalConstants.FAKE_PASSWORD;
                        binding.setUser(selectedUser);
                        if (selectedUser.teams != null)
                            binding.teamsInUserSpinner.setSelection(createItemsFromTeams(selectedUser.teams));
                        else
                            binding.teamsInUserSpinner.resetSelection();
                        if (selectedUser.tasks != null)
                            binding.tasksInUserSpinner.setSelection(createItemsFromTasks(selectedUser.tasks));
                        else
                            binding.tasksInUserSpinner.resetSelection();
                    }
                    if (selectedItem.getObj() == null) {
                        resetView();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void observeViewModel(View view) {
        viewModel.getTeamsLiveData().observe(getViewLifecycleOwner(), teams -> {
            ArrayList<Item> items = new ArrayList<>();
            if (teams != null) {
                for (Team team : teams) {
                    items.add(Item.builder().name(team.getName()).value(false).obj(team).build());
                }
            }
            binding.teamsInUserSpinner.setItems(items);
        });

        viewModel.getTasksLiveData().observe(getViewLifecycleOwner(), tasks -> {
            ArrayList<Item> items = new ArrayList<>();
            if (tasks != null) {
                for (Task task : tasks) {
                    items.add(Item.builder().name(task.getTitle()).value(false).obj(task).build());
                }
            }
            binding.tasksInUserSpinner.setItems(items);


        });

        viewModel.getUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            ArrayList<Item> items = new ArrayList<>();
            if (users != null) {
                for (UserRegisterDetails user : users) {
                    items.add(Item.builder().name(user.firstName + " " + user.lastName).value(false).obj(user).build());
                }
            }
            adapter.clear();
            adapter.add(Item.builder().name("").value(false).obj(null).build());
            adapter.addAll(items);
            adapter.notifyDataSetChanged();
        });


        viewModel.getIsUserCreated().observe(getViewLifecycleOwner(), created -> {
            if (created != null) {
                setToTeamView();
                if (created.getOperationResult().equals(OperationResponseStatus.SUCCESS.name())) {
                    viewModel.refreshUsers();
                    Snackbar.make(view, "User Created Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (created.getOperationResult().equals(OperationResponseStatus.ERROR.name())) {
                    Snackbar.make(view, created.getResponseMessage(), Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsUserUpdated().observe(getViewLifecycleOwner(), updated -> {
            if (updated != null) {
                setToTeamView();
                if (updated.getOperationResult().equals(OperationResponseStatus.SUCCESS.name())) {
                    viewModel.refreshUsers();
                    Snackbar.make(view, "User Updated Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (updated.getOperationResult().equals(OperationResponseStatus.ERROR.name())) {
                    Snackbar.make(view, updated.getResponseMessage(), Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsUserDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (deleted != null) {
                setToTeamView();
                if (deleted.getOperationResult().equals(OperationResponseStatus.SUCCESS.name())) {
                    viewModel.refreshUsers();
                    Snackbar.make(view, "User Deleted Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (deleted.getOperationResult().equals(OperationResponseStatus.ERROR.name())) {
                    Snackbar.make(view, deleted.getResponseMessage(), Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.userLoadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    setToLoadingView();
                }
            }
        });

    }

    private UserRegisterDetails checkInputForDelete(FragmentAddUserBinding binding) {
        if (binding.selectUserSpinner.getSelectedItem() != null) {
            Item selectedItem = (Item) binding.selectUserSpinner.getSelectedItem();
            if (selectedItem.getObj() instanceof UserRegisterDetails) {
                return (UserRegisterDetails) selectedItem.getObj();
            }
        }
        return null;
    }

    private UserRegisterDetails checkInputAndCreateUser(FragmentAddUserBinding binding, boolean toUpdate) {
        boolean validInput = true;

        String email = Objects.requireNonNull(binding.editTextEmail.getText()).toString().trim();
        String firstName = Objects.requireNonNull(binding.editTextFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTextLastName.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTextPassword.getText()).toString().trim();
        Boolean isManager = binding.isManager.isChecked();

        if (email.isEmpty()) {
            binding.editTextEmail.setError("Enter a valid email");
            binding.editTextEmail.requestFocus();
            validInput = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Enter a valid email");
            binding.editTextEmail.requestFocus();
            validInput = false;
        }
        if (password.isEmpty()) {
            binding.editTextPassword.setError("Password required");
            binding.editTextPassword.requestFocus();
            validInput = false;
        }
        if (password.length() < 6) {
            binding.editTextPassword.setError("Password should be at least 6 characters long");
            binding.editTextPassword.requestFocus();
            validInput = false;
        }
        if (firstName.isEmpty()) {
            binding.editTextFirstName.setError("First name required");
            binding.editTextFirstName.requestFocus();
            validInput = false;
        }
        if (lastName.isEmpty()) {
            binding.editTextLastName.setError("Last name required");
            binding.editTextLastName.requestFocus();
            validInput = false;
        }


        if (validInput) {
            String userId = null;
            ArrayList<Team> teams = new ArrayList<>();
            for (Item item : binding.teamsInUserSpinner.getSelectedItems())
                teams.add((Team) item.getObj());

            ArrayList<Task> tasks = new ArrayList<>();
            for (Item item : binding.tasksInUserSpinner.getSelectedItems())
                tasks.add((Task) item.getObj());

            if (toUpdate) {
                if (password.equals(GlobalConstants.FAKE_PASSWORD))
                    password = null;

                if (binding.selectUserSpinner.getSelectedItem() != null) {
                    Item selectedItem = (Item) binding.selectUserSpinner.getSelectedItem();
                    if (selectedItem.getObj() instanceof UserRegisterDetails) {
                        userId = ((UserRegisterDetails) selectedItem.getObj()).userId;
                    }
                }
            }

            return new UserRegisterDetails(userId, firstName, lastName, password, email, isManager, teams, tasks);
        } else {
            return null;
        }

    }

    private ArrayList<Item> createItemsFromTeams(List<Team> teams) {
        ArrayList<Item> items = new ArrayList<>();
        if (teams != null) {
            for (Team team : teams) {
                items.add(Item.builder().name(team.getName()).value(false).obj(team).build());
            }
        }
        return items;
    }

    private ArrayList<Item> createItemsFromTasks(List<Task> tasks) {
        ArrayList<Item> items = new ArrayList<>();
        if (tasks != null) {
            for (Task task : tasks) {
                items.add(Item.builder().name(task.getTitle()).value(false).obj(task).build());
            }
        }
        return items;
    }

    private void resetView() {
        binding.saveUser.setClickable(true);
        binding.setUser(new UserRegisterDetails());
        binding.selectUserSpinner.setSelection(0);
        binding.teamsInUserSpinner.resetSelection();
        binding.tasksInUserSpinner.resetSelection();
    }

    private void setToTeamView() {
        binding.userLoadingView.setVisibility(View.GONE);
        binding.userBody.setVisibility(View.VISIBLE);
        binding.userButtonsLayout.setVisibility(View.VISIBLE);
        binding.selectUserSpinnerLayout.setVisibility(View.VISIBLE);
    }

    private void setToLoadingView() {
        binding.userBody.setVisibility(View.GONE);
        binding.userButtonsLayout.setVisibility(View.GONE);
        binding.selectUserSpinnerLayout.setVisibility(View.GONE);
    }
}