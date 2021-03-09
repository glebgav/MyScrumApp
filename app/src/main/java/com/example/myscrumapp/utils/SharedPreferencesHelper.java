package com.example.myscrumapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.example.myscrumapp.model.entity.LoggedInUser;

public class SharedPreferencesHelper {

    private static final String PREF_TIME = "Pref time";
    private static final String USER_ID = "userId";
    private static final String USER_FIRST_NAME = "firstName";
    private static final String USER_EMAIL = "email";
    private static final String TOKEN = "token";
    private static final String IS_MANAGER = "isManager";
    private static final String DEFAULT_NONE = "NONE";



    private static SharedPreferencesHelper instance;
    private final SharedPreferences preferences;

    private SharedPreferencesHelper(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferencesHelper getInstance(Context context){
        if(instance == null)
            instance = new SharedPreferencesHelper(context);
        return instance;
    }

    public void saveUpdateTime(Long time){
        preferences.edit().putLong(PREF_TIME, time).apply();
    }

    public Long getUpdateTime(){
        return preferences.getLong(PREF_TIME,0);
    }

    public void saveUser(LoggedInUser loggedInUser){
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(USER_FIRST_NAME,loggedInUser.firstName);
        editor.putString(USER_ID,loggedInUser.userId);
        editor.putString(USER_EMAIL,loggedInUser.email);
        editor.putString(TOKEN,loggedInUser.token);
        editor.putBoolean(IS_MANAGER,loggedInUser.isManager);

        editor.apply();

    }

    public boolean isLoggedIn(){
        return !preferences.getString(USER_ID, DEFAULT_NONE).equals(DEFAULT_NONE);
    }

    public LoggedInUser getUser(){
        return new LoggedInUser(preferences.getString(USER_FIRST_NAME, DEFAULT_NONE),
                preferences.getString(USER_ID, DEFAULT_NONE),
                preferences.getString(USER_EMAIL, DEFAULT_NONE),
                preferences.getString(TOKEN, DEFAULT_NONE),
                preferences.getBoolean(IS_MANAGER, false));
    }

    public void clear(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
