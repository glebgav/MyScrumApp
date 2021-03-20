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
import com.example.myscrumapp.databinding.FragmentAddTaskBinding;
import com.example.myscrumapp.model.entity.Item;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.TeamInTask;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.utils.GlobalConstants;
import com.example.myscrumapp.viewmodel.AddTaskViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Objects;


public class AddTaskFragment extends Fragment {
    private FragmentAddTaskBinding binding;
    private AddTaskViewModel viewModel;
    private ArrayAdapter<Item> adapter;

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

        adapter = new ArrayAdapter<>(getContext(), R.layout.custom_spinner);
        adapter.add(Item.builder().name("").value(false).obj(null).build());


        ArrayList<Item> items = new ArrayList<>();
        items.add(Item.builder().name("To Do").value(false).obj(GlobalConstants.TODO_STATUS).build());
        items.add(Item.builder().name("In Progress").value(false).obj(GlobalConstants.IN_PROGRESS_STATUS).build());
        items.add(Item.builder().name("Done").value(false).obj(GlobalConstants.DONE_STATUS).build());

        binding.statusSpinner.setItems(items);

        binding.selectTaskSpinner.setAdapter(adapter);

        configureListeners();

        observeViewModel(view);
    }

    private void configureListeners() {
        binding.saveTask.setOnClickListener(v -> {
            Task taskToCreate = checkInputAndCreateTask(binding, v, false);
            if (taskToCreate != null) {
                viewModel.setIsLoading(true);
                viewModel.addTask(taskToCreate);
            }

        });

        binding.editTask.setOnClickListener(v -> {
            Task taskToUpdate = checkInputAndCreateTask(binding, v, true);
            if (taskToUpdate != null) {
                viewModel.setIsLoading(true);
                viewModel.updateTask(taskToUpdate);
            }

        });

        binding.deleteTask.setOnClickListener(v -> {
            Task taskToDelete = checkInputForDelete(binding);
            if (taskToDelete != null) {
                viewModel.setIsLoading(true);
                viewModel.deleteTask(taskToDelete);
            }

        });

        binding.selectTaskSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (binding.selectTaskSpinner.getSelectedItem() != null) {
                    Item selectedItem = (Item) binding.selectTaskSpinner.getSelectedItem();
                    if (selectedItem.getObj() instanceof Task) {
                        binding.saveTask.setClickable(false);
                        Task selectedTask = (Task) selectedItem.getObj();
                        binding.setTask(selectedTask);
                        if (selectedTask.getUserDetails() != null)
                            binding.userInTaskSpinner.setSelectedItem(selectedTask.getUserDetails().userId);
                        else
                            binding.userInTaskSpinner.setSelectedItem(GlobalConstants.NON_EXISTENT_ID);
                        if (selectedTask.getTeamDetails() != null)
                            binding.teamInTaskSpinner.setSelectedItem(selectedTask.getTeamDetails().teamId);
                        else
                            binding.teamInTaskSpinner.setSelectedItem(GlobalConstants.NON_EXISTENT_ID);
                        binding.statusSpinner.setSelectedItem(selectedTask.getStatus());
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
        viewModel.getUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            ArrayList<Item> items = new ArrayList<>();
            if (users != null) {
                for (User user : users) {
                    items.add(Item.builder().name(user.firstName + " " + user.lastName).value(false).obj(user).build());
                }
            }
            binding.userInTaskSpinner.setItems(items);
        });

        viewModel.getTeamsLiveData().observe(getViewLifecycleOwner(), teams -> {
            ArrayList<Item> items = new ArrayList<>();
            if (teams != null) {
                for (Team team : teams) {
                    items.add(Item.builder().name(team.getName()).value(false).obj(team).build());
                }
            }
            binding.teamInTaskSpinner.setItems(items);


        });

        viewModel.getTasksLiveData().observe(getViewLifecycleOwner(), tasks -> {
            ArrayList<Item> items = new ArrayList<>();
            if (tasks != null) {
                for (Task task : tasks) {
                    items.add(Item.builder().name(task.getTitle()).value(false).obj(task).build());
                }

            }
            adapter.clear();
            adapter.add(Item.builder().name("").value(false).obj(null).build());
            adapter.addAll(items);
            adapter.notifyDataSetChanged();
        });
        viewModel.getIsTaskCreated().observe(getViewLifecycleOwner(), created -> {
            if (created != null) {
                setToTaskView();
                if (created) {
                    viewModel.refreshTasks();
                    Snackbar.make(view, "Task Created Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (!created) {
                    Snackbar.make(view, "Error Creating the Task", Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsTaskDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (deleted != null) {
                setToTaskView();
                if (deleted) {
                    viewModel.refreshTasks();
                    resetView();
                    Snackbar.make(view, "Task Deleted Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (!deleted) {
                    Snackbar.make(view, "Error Deleting the Task", Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsTaskUpdated().observe(getViewLifecycleOwner(), updated -> {
            if (updated != null) {
                setToTaskView();
                if (updated) {
                    viewModel.refreshTasks();
                    resetView();
                    Snackbar.make(view, "Task Updated Successfully", Snackbar.LENGTH_LONG).show();
                }
                if (!updated) {
                    Snackbar.make(view, "Error Updating the Task", Snackbar.LENGTH_LONG).show();
                }
            }

        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.taskLoadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    setToLoadingView();
                }
            }
        });

    }

    private Task checkInputForDelete(FragmentAddTaskBinding binding) {
        if (binding.selectTaskSpinner.getSelectedItem() != null) {
            Item selectedItem = (Item) binding.selectTaskSpinner.getSelectedItem();
            if (selectedItem.getObj() instanceof Task) {
                return (Task) selectedItem.getObj();
            }
        }
        return null;
    }

    private Task checkInputAndCreateTask(FragmentAddTaskBinding binding, View view, boolean toUpdate) {
        boolean validInput = true;
        String taskTitle = Objects.requireNonNull(binding.editTextTitle.getText()).toString().trim();
        String taskDescription = Objects.requireNonNull(binding.editTextDescription.getText()).toString().trim();


        if (taskTitle.isEmpty()) {
            binding.editTextTitle.setError("Enter a valid task title");
            binding.editTextTitle.requestFocus();
            validInput = false;
        }

        if (taskDescription.isEmpty()) {
            binding.editTextDescription.setError("Enter a valid task description");
            binding.editTextDescription.requestFocus();
            validInput = false;
        }

        if (binding.statusSpinner.getSelectedItem() == null) {
            Snackbar.make(view, "Need task status", Snackbar.LENGTH_LONG).show();
            validInput = false;
        }

        if (validInput) {
            User user = null;
            TeamInTask teamInTask = null;
            String taskId = null;

            int status = (int) binding.statusSpinner.getSelectedItem().getObj();

            if (binding.userInTaskSpinner.getSelectedItem() != null)
                user = (User) binding.userInTaskSpinner.getSelectedItem().getObj();

            if (binding.teamInTaskSpinner.getSelectedItem() != null) {
                Team team = (Team) binding.teamInTaskSpinner.getSelectedItem().getObj();
                teamInTask = new TeamInTask();
                teamInTask.teamId = team.getTeamId();
            }


            if (toUpdate) {
                if (binding.selectTaskSpinner.getSelectedItem() != null) {
                    Item selectedItem = (Item) binding.selectTaskSpinner.getSelectedItem();
                    if (selectedItem.getObj() instanceof Task) {
                        taskId = ((Task) selectedItem.getObj()).getTaskId();
                    }
                }
            }


            return new Task(taskId, taskTitle, taskDescription, status, user, teamInTask);
        } else {
            return null;
        }

    }

    private void resetView() {
        binding.saveTask.setClickable(true);
        binding.setTask(new Task());
        binding.selectTaskSpinner.setSelection(0);
        binding.userInTaskSpinner.setSelectedItem(GlobalConstants.NON_EXISTENT_ID);
        binding.teamInTaskSpinner.setSelectedItem(GlobalConstants.NON_EXISTENT_ID);
        binding.statusSpinner.setSelectedItem(GlobalConstants.INVALID_STATUS);
    }

    private void setToTaskView() {
        binding.taskLoadingView.setVisibility(View.GONE);
        binding.taskBody.setVisibility(View.VISIBLE);
        binding.taskButtonsLayout.setVisibility(View.VISIBLE);
        binding.selectTaskSpinnerLayout.setVisibility(View.VISIBLE);
    }

    private void setToLoadingView() {
        binding.taskBody.setVisibility(View.GONE);
        binding.taskButtonsLayout.setVisibility(View.GONE);
        binding.selectTaskSpinnerLayout.setVisibility(View.GONE);
    }
}