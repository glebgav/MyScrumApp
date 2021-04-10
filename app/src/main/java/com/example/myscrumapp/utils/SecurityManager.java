package com.example.myscrumapp.utils;

import android.content.Context;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.entity.User;
/**
 *  Security manager for the app
 */
public class SecurityManager {
    private static SecurityManager instance;
    private final SharedPreferencesHelper preferences;

    private SecurityManager(Context context){
        preferences = SharedPreferencesHelper.getInstance(context);
    }

    public static SecurityManager getInstance(Context context){
        if(instance == null)
            instance = new SecurityManager(context);
        return instance;
    }

    /**
     * @return if current logged in user is manager
     */
    public boolean isManager(){
        return preferences.getUser().isManager;
    }

    /**
     * @param task task to modify
     * @return if current user can modify this task
     */
    public  boolean canModifyTask(Task task){
        User taskUser = task.getUserDetails();
        if(taskUser != null)
            return isManager() || task.getUserDetails().userId.equals(preferences.getUser().userId);
        else return isManager();
    }


}
