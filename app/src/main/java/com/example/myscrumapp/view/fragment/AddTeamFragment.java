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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.myscrumapp.R;
import com.example.myscrumapp.databinding.FragmentAddTeamBinding;
import com.example.myscrumapp.model.entity.Item;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.TeamToCreate;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.example.myscrumapp.viewmodel.AddTeamViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddTeamFragment extends Fragment {
    private FragmentAddTeamBinding binding;
    private AddTeamViewModel viewModel;
    private ArrayAdapter<Item> adapter;
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

        adapter = new ArrayAdapter<>(getContext(), R.layout.custom_spinner);
        adapter.add(Item.builder().name("").value(false).obj(null).build());

        binding.selectTeamSpinner.setAdapter(adapter);


        configureListeners();

        observeViewModel(view);
    }

    private void configureListeners() {
        binding.saveTeam.setOnClickListener(v -> {
            Team teamToCreate = checkInputAndCreateTeam(binding,false);
            if (teamToCreate != null) {
                viewModel.setIsLoading(true);
                viewModel.addTeam(teamToCreate);
            }

        });

        binding.editTeam.setOnClickListener(v -> {
            Team teamToUpdate = checkInputAndCreateTeam(binding, true);
            if (teamToUpdate != null) {
                viewModel.setIsLoading(true);
                viewModel.updateTeam(teamToUpdate);
            }

        });

        binding.deleteTeam.setOnClickListener(v -> {
            Team teamToDelete = checkInputForDelete(binding);
            if (teamToDelete != null) {
                viewModel.setIsLoading(true);
                viewModel.deleteTeam(teamToDelete);
            }

        });

        binding.selectTeamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (binding.selectTeamSpinner.getSelectedItem() != null) {
                    Item selectedItem = (Item) binding.selectTeamSpinner.getSelectedItem();
                    if (selectedItem.getObj() instanceof Team) {
                        binding.saveTeam.setClickable(false);
                        Team selectedTeam = (Team) selectedItem.getObj();
                        binding.setTeam(selectedTeam);
                        if (selectedTeam.getUsers() != null)
                            binding.usersInTeamListSpinner.setSelection(createItemsFromUsers(selectedTeam.getUsers()));
                        else
                            binding.usersInTeamListSpinner.resetSelection();
                        if (selectedTeam.getTasks() != null)
                            binding.tasksInTeamListSpinner.setSelection(createItemsFromTasks(selectedTeam.getTasks()));
                        else
                            binding.tasksInTeamListSpinner.resetSelection();
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

    private Team checkInputForDelete(FragmentAddTeamBinding binding) {
        if (binding.selectTeamSpinner.getSelectedItem() != null) {
            Item selectedItem = (Item) binding.selectTeamSpinner.getSelectedItem();
            if (selectedItem.getObj() instanceof Team) {
                return (Team) selectedItem.getObj();
            }
        }
        return null;
    }

    private void observeViewModel(View view) {
        viewModel.getUsersLiveData().observe(getViewLifecycleOwner(), users ->
                binding.usersInTeamListSpinner.setItems(createItemsFromUsers(users)));

        viewModel.getTasksLiveData().observe(getViewLifecycleOwner(), tasks ->
            binding.tasksInTeamListSpinner.setItems(createItemsFromTasks(tasks)));

        viewModel.getTeamsLiveData().observe(getViewLifecycleOwner(), teams -> {
            ArrayList<Item> items = new ArrayList<>();
            if (teams != null) {
                for (Team team : teams) {
                    items.add(Item.builder().name(team.getName()).value(false).obj(team).build());
                }
            }
            adapter.clear();
            adapter.add(Item.builder().name("").value(false).obj(null).build());
            adapter.addAll(items);
            adapter.notifyDataSetChanged();
        });


        viewModel.getIsTeamCreated().observe(getViewLifecycleOwner(), created -> {
            if (created != null) {
                setToTeamView();
                if (created) {
                    Snackbar.make(view, "Team Created Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (!created) {
                    Snackbar.make(view, "Error Creating the Team", Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsTeamDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (deleted != null) {
                setToTeamView();
                if (deleted) {
                    Snackbar.make(view, "Team Deleted Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (!deleted) {
                    Snackbar.make(view, "Error Deleting the Team", Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsTeamUpdated().observe(getViewLifecycleOwner(), updated -> {
            if (updated != null) {
                setToTeamView();
                if (updated) {
                    Snackbar.make(view, "Team Updated Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (!updated) {
                    Snackbar.make(view, "Error Updating the Team", Snackbar.LENGTH_LONG).show();
                }
            }

        });


        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.teamLoadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    setToLoadingView();
                }
            }
        });

    }

    private Team checkInputAndCreateTeam(FragmentAddTeamBinding binding, boolean toUpdate) {
        boolean validInput = true;

        String teamName = Objects.requireNonNull(binding.editTextTeamName.getText()).toString().trim();


        if (teamName.isEmpty()) {
            binding.editTextTeamName.setError("Enter a valid team name");
            binding.editTextTeamName.requestFocus();
            validInput = false;
        }

        if (validInput) {
            String teamId = null;
            ArrayList<User> users = new ArrayList<>();
            for (Item item : binding.usersInTeamListSpinner.getSelectedItems())
                users.add((User) item.getObj());

            ArrayList<Task> tasks = new ArrayList<>();
            for (Item item : binding.tasksInTeamListSpinner.getSelectedItems())
                tasks.add((Task) item.getObj());


            if (toUpdate) {
                if (binding.selectTeamSpinner.getSelectedItem() != null) {
                    Item selectedItem = (Item) binding.selectTeamSpinner.getSelectedItem();
                    if (selectedItem.getObj() instanceof Team) {
                        teamId = ((Team) selectedItem.getObj()).getTeamId();
                    }
                }
            }

            return new Team(teamId, teamName, tasks, users);
        } else {
            return null;
        }

    }

    private ArrayList<Item> createItemsFromUsers(List<User> users) {
        ArrayList<Item> items = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                items.add(Item.builder().name(user.firstName + " " + user.lastName).value(false).obj(user).build());
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
        binding.saveTeam.setClickable(true);
        binding.setTeam(new Team());
        binding.selectTeamSpinner.setSelection(0);
        binding.usersInTeamListSpinner.resetSelection();
        binding.tasksInTeamListSpinner.resetSelection();
    }

    private void setToTeamView() {
        binding.teamLoadingView.setVisibility(View.GONE);
        binding.teamBody.setVisibility(View.VISIBLE);
        binding.teamButtonsLayout.setVisibility(View.VISIBLE);
        binding.selectTeamSpinnerLayout.setVisibility(View.VISIBLE);
    }

    private void setToLoadingView() {
        binding.teamBody.setVisibility(View.GONE);
        binding.teamButtonsLayout.setVisibility(View.GONE);
        binding.selectTeamSpinnerLayout.setVisibility(View.GONE);
    }
}