package com.example.myscrumapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.example.myscrumapp.model.entity.LoggedInUser;
/**
 *  Helper Class for managing shared data that stored on phone storage securely
 */
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

    /**
     * save time of last local Db refresh
     * @param time time that local Db is refreshed
     */
    public void saveUpdateTime(Long time){
        preferences.edit().putLong(PREF_TIME, time).apply();
    }

    /**
     * get time of last local Db refresh
     * @return time of last local Db refresh
     */
    public Long getUpdateTime(){
        return preferences.getLong(PREF_TIME,0);
    }

    /**
     * save logged in user details
     * @param loggedInUser logged in user details
     */
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

    /**
     * get logged in user details, including authorization token
     * @return logged in user details
     */
    public LoggedInUser getUser(){
        return new LoggedInUser(preferences.getString(USER_FIRST_NAME, DEFAULT_NONE),
                preferences.getString(USER_ID, DEFAULT_NONE),
                preferences.getString(USER_EMAIL, DEFAULT_NONE),
                preferences.getString(TOKEN, DEFAULT_NONE),
                preferences.getBoolean(IS_MANAGER, false));
    }

    /**
     * clear all shared preferences , used in a logout scenario
     */
    public void clear(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
