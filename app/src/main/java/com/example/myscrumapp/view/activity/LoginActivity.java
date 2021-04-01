package com.example.myscrumapp.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myscrumapp.R;
import com.example.myscrumapp.model.entity.LoggedInUser;
import com.example.myscrumapp.model.entity.UserLoginDetails;
import com.example.myscrumapp.model.network.ApiService;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.google.android.material.snackbar.Snackbar;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword;
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.emailLogin);
        editTextPassword = findViewById(R.id.passwordLogin);

        findViewById(R.id.buttonLogin).setOnClickListener(this);
        findViewById(R.id.textViewRegister).setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLogin:
                userLogin(v);
                break;
            case R.id.textViewRegister:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
        }
    }

    private void userLogin(View v) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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

        disposable.add(
                ApiService.getInstance().getUsersApi().userLogin(new UserLoginDetails(password, email))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull Response<ResponseBody> response) {
                                if (response.code() == 200) {
                                    String userId = response.headers().get("UserId");
                                    String firstName = response.headers().get("UserName");
                                    String token = response.headers().get("Authorization");
                                    Boolean isManager = Boolean.parseBoolean(response.headers().get("isManager"));

                                    SharedPreferencesHelper.getInstance(getApplicationContext()).saveUser(new LoggedInUser(firstName, userId, email, token, isManager));

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    Snackbar.make(v, "Can't login, please check email or password", Snackbar.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }));
    }
}