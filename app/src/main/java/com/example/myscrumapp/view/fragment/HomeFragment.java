package com.example.myscrumapp.view.fragment;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.myscrumapp.R;
import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import butterknife.ButterKnife;


public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LoggedInUser user = SharedPreferencesHelper.getInstance(getContext()).getUser();
        setText("Welcome "+user.firstName+" !");

    }

    public void setText(String text){
        TextView textView = requireView().findViewById(R.id.welcomeBanner);
        textView.setText(text);
    }
}
