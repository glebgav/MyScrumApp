package com.example.myscrumapp.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscrumapp.R;
import com.example.myscrumapp.databinding.ItemTaskBinding;
import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.view.listener.TaskDetailsListener;
import com.example.myscrumapp.view.fragment.ListFragmentDirections;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> implements TaskDetailsListener {

    private ArrayList<Task> tasksList;

    public TaskListAdapter(ArrayList<Task> tasksList){
        this.tasksList = tasksList;
    }

    public void updateTasksList(List<Task> newTaskList){
        tasksList.clear();
        tasksList.addAll(newTaskList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = LayoutInflater.from(parent.getContext());
        ItemTaskBinding view = DataBindingUtil.inflate(inflater,R.layout.item_task,parent,false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.itemView.setTask(tasksList.get(position));
        holder.itemView.setListener(this);
/*        TextView title = holder.itemView.findViewById(R.id.taskTitle);
        TextView description = holder.itemView.findViewById(R.id.taskDescription);
        TextView assignedUser = holder.itemView.findViewById(R.id.assignedUser);
        LinearLayout layout = holder.itemView.findViewById(R.id.taskLayout);

        title.setText(tasksList.get(position).getTitle());
        description.setText(tasksList.get(position).getDescription());
        assignedUser.setText(tasksList.get(position).getDescription());
        layout.setOnClickListener(v -> {
            ListFragmentDirections.ActionTaskDetail action = ListFragmentDirections.actionTaskDetail();
            action.setTaskUuid(tasksList.get(position).getTaskId());
            Navigation.findNavController(layout).navigate(action);
        });*/
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }

    @Override
    public void onTaskDetailsClicked(View v) {
        String UuidString = ((TextView)v.findViewById(R.id.taskId)).getText().toString();

        ListFragmentDirections.ActionTaskDetail action = ListFragmentDirections.actionTaskDetail();
        action.setTaskUuid(UuidString);
        Navigation.findNavController(v).navigate(action);

    }

    class TaskViewHolder extends RecyclerView.ViewHolder{

        public ItemTaskBinding itemView;

        public TaskViewHolder(@NonNull ItemTaskBinding itemView) {
            super(itemView.getRoot());
            this.itemView = itemView;
        }
    }
}
