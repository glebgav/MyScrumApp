package com.example.myscrumapp.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.example.myscrumapp.R;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration mAppBarConfiguration;
    private ExtendedFloatingActionButton addUser;
    private ExtendedFloatingActionButton addTeam;
    private ExtendedFloatingActionButton addTask;
    private boolean isFABOpen =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());

        FloatingActionButton fab = findViewById(R.id.fabOpenMenu);
        if (sharedPreferencesHelper.isLoggedIn()){
            if(!sharedPreferencesHelper.getUser().isManager){
                fab.setVisibility(View.GONE);
            }
        }
        addUser = findViewById(R.id.fabAddUser);
        addTeam = findViewById(R.id.fabAddTeam);
        addTask = findViewById(R.id.fabAddTask);

        addUser.setVisibility(View.GONE);
        addTeam.setVisibility(View.GONE);
        addTask.setVisibility(View.GONE);


        fab.setOnClickListener(view -> {
            if(!isFABOpen){
                showFABMenu();
            }else{
                closeFABMenu();
            }
        });



/*        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());*/


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.header_userName);
        TextView navUserEmail = headerView.findViewById(R.id.header_userEmail);
        navUsername.setText(sharedPreferencesHelper.getUser().firstName);
        navUserEmail.setText(sharedPreferencesHelper.getUser().email);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.teamListFragment,R.id.taskListFragment)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this,R.id.fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        addUser.setOnClickListener(view -> {
            navController.navigate(R.id.addUserFragment);
            closeFABMenu();
        });

        addTeam.setOnClickListener(view -> {
            navController.navigate(R.id.addTeamFragment);
            closeFABMenu();
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.action_logout) {
            SharedPreferencesHelper.getInstance(getApplicationContext()).clear();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!SharedPreferencesHelper.getInstance(getApplicationContext()).isLoggedIn())
        {
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        }
    }

    private void showFABMenu(){
        isFABOpen=true;
        addUser.setVisibility(View.VISIBLE);
        addTeam.setVisibility(View.VISIBLE);
        addTask.setVisibility(View.VISIBLE);
        addUser.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
        addTeam.animate().translationY(-getResources().getDimension(R.dimen.standard_125));
        addTask.animate().translationY(-getResources().getDimension(R.dimen.standard_185));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        addUser.animate().translationY(0);
        addTeam.animate().translationY(0);
        addTask.animate().translationY(0);

        addUser.setVisibility(View.GONE);
        addTeam.setVisibility(View.GONE);
        addTask.setVisibility(View.GONE);
    }
}