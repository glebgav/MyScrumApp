package com.example.myscrumapp.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myscrumapp.R;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.network.ApiService;
import com.google.android.material.snackbar.Snackbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        loadingProgressBar = findViewById(R.id.loading);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);

        findViewById(R.id.buttonSignUp).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonSignUp:
                userSignUp(v);
                break;
            case R.id.textViewLogin:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }

    }

    private void userSignUp(View v) {
        String email = editTextEmail.getText().toString().trim();
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(email.isEmpty()){
            editTextEmail.setError("Enter a valid email");
            editTextEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Enter a valid email");
            editTextEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            editTextPassword.setError("Password required");
            editTextPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            editTextPassword.setError("Password should be at least 6 characters long");
            editTextPassword.requestFocus();
            return;
        }
        if(firstName.isEmpty()){
            editTextFirstName.setError("First name required");
            editTextFirstName.requestFocus();
            return;
        }
        if(lastName.isEmpty()){
            editTextLastName.setError("Last name required");
            editTextLastName.requestFocus();
            return;
        }
        /*Do user registration*/
        UserRegisterDetails userRegisterDetails = new UserRegisterDetails(null, firstName, lastName,password,email, true,null,null);
        loadingProgressBar.setVisibility(View.VISIBLE);
        Call<UserRegisterDetails> call = ApiService.getInstance()
                .getUsersApi()
                .createUser(userRegisterDetails);

        call.enqueue(new Callback<UserRegisterDetails>() {
            @SneakyThrows
            @Override
            public void onResponse(@NonNull Call<UserRegisterDetails> call, @NonNull Response<UserRegisterDetails> response) {

                if(response.code() == 200){
                    Snackbar.make(v, "User Created Successfully", Snackbar.LENGTH_LONG).show();
                }else{
                    Snackbar.make(v, "Error  while creating the user ", Snackbar.LENGTH_LONG).show();
                }
                loadingProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NonNull Call<UserRegisterDetails> call, @NonNull Throwable t) {
                Toast.makeText(SignUpActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingProgressBar.setVisibility(View.GONE);
            }

        });

    }


}