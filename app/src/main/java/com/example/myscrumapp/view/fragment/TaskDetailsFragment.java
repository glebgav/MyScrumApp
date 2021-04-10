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
import com.example.myscrumapp.databinding.FragmentTaskDetailBinding;
import com.example.myscrumapp.utils.GlobalConstants;
import com.example.myscrumapp.viewmodel.TaskDetailsViewModel;
/**
 *  Fragment task details
 */
public class TaskDetailsFragment extends Fragment {

    private String taskUuid;
    private TaskDetailsViewModel viewModel;
    private FragmentTaskDetailBinding binding;

    public TaskDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_task_detail,container,false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getArguments() != null){
            taskUuid = TaskDetailsFragmentArgs.fromBundle(getArguments()).getTaskUuid();
        }

        viewModel = ViewModelProviders.of(this).get(TaskDetailsViewModel.class);
        viewModel.getTask(taskUuid);

        observeViewModel();

    }

    private void observeViewModel() {
        viewModel.getTaskMutableLiveData().observe(getViewLifecycleOwner(), task -> {
            if(task != null){
                binding.setTask(task);
                String myStatus = "Status: "+GlobalConstants.taskStatsToTextMap.get(task.getStatus());
                binding.taskStatusDetails.setText(myStatus);

            }
        });
    }


}