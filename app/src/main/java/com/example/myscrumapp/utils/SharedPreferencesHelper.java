package com.example.myscrumapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SharedPreferencesHelper {

    private static final String PREF_TIME = "Pref time";
    private static SharedPreferencesHelper instance;
    private SharedPreferences preferences;

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
}
