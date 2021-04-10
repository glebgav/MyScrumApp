package com.example.myscrumapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myscrumapp.R;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.model.network.OperationResponseModel;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 *  Activity for Sign-up action
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName;
    private ProgressBar loadingProgressBar;
    private MaterialCheckBox isMangerCheckBox;
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        loadingProgressBar = findViewById(R.id.loading);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        isMangerCheckBox = findViewById(R.id.is_manager_checkbox);

        findViewById(R.id.buttonSignUp).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
        boolean isManager = isMangerCheckBox.isChecked();

        if (email.isEmpty()) {
            editTextEmail.setError("Enter a valid email");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password required");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password should be at least 6 characters long");
            editTextPassword.requestFocus();
            return;
        }
        if (firstName.isEmpty()) {
            editTextFirstName.setError("First name required");
            editTextFirstName.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            editTextLastName.setError("Last name required");
            editTextLastName.requestFocus();
            return;
        }
        /*Do user registration*/
        UserRegisterDetails userRegisterDetails = new UserRegisterDetails(null, firstName, lastName, password, email, isManager, null, null);
        loadingProgressBar.setVisibility(View.VISIBLE);

        disposable.add(
                ApiService.getInstance().getUsersApi().createUser(userRegisterDetails)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<UserRegisterDetails>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull UserRegisterDetails createdUser) {
                                Snackbar.make(v, "User Created Successfully", Snackbar.LENGTH_LONG).show();
                                loadingProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                Snackbar.make(v, OperationResponseModel.failedResponse("Add",e).getResponseMessage(), Snackbar.LENGTH_LONG).show();
                                e.printStackTrace();
                                loadingProgressBar.setVisibility(View.GONE);
                            }
                        })
        );

    }

}