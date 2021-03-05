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
import com.example.myscrumapp.databinding.ItemTeamBinding;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.view.fragment.TaskListFragmentDirections;
import com.example.myscrumapp.view.fragment.TeamListFragmentDirections;
import com.example.myscrumapp.view.listener.TaskListListener;
import java.util.ArrayList;
import java.util.List;

public class TeamListAdapter extends RecyclerView.Adapter<TeamListAdapter.TeamViewHolder> implements TaskListListener {

    private ArrayList<Team> teamsList;

    public TeamListAdapter(ArrayList<Team> teamsList){
        this.teamsList = teamsList;
    }

    public void updateTeamsList(List<Team> newTeamList){
        teamsList.clear();
        teamsList.addAll(newTeamList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = LayoutInflater.from(parent.getContext());
        ItemTeamBinding view = DataBindingUtil.inflate(inflater,R.layout.item_team,parent,false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        holder.itemView.setTeam(teamsList.get(position));
        holder.itemView.setListener(this);
    }

    @Override
    public int getItemCount() {
        return teamsList.size();
    }


    @Override
    public void onTeamDetailsClicked(View v) {
        Navigation.findNavController(v).navigate(TeamListFragmentDirections.actionTeamListFragmentToTaskListFragment());
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder{

        public ItemTeamBinding itemView;

        public TeamViewHolder(@NonNull ItemTeamBinding itemView) {
            super(itemView.getRoot());
            this.itemView = itemView;
        }
    }
}
