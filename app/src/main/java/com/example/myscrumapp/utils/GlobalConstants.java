package com.example.myscrumapp.utils;

import java.util.HashMap;
import java.util.HashSet;
/**
 *  All global constants of the app
 */
public class GlobalConstants {
    public static final Long REFRESH_TIME = 5*60*100*1000*1000L;
    public static final String MY_TASKS_FRAGMENT_INDICATOR = "MyTasks";
    public static final int TODO_STATUS = 0;
    public static final int IN_PROGRESS_STATUS = 1;
    public static final int DONE_STATUS = 2;
    public static final int INVALID_STATUS = -1;
    public static final String NON_EXISTENT_ID = "NONE";
    public static final String FAKE_PASSWORD = "FAKE_PASSWORD";
    public static final String API_SERVICE_URL = "http://192.168.1.6:8080";


    /**
     *  hash map of status(Integer) -> status(String)
     */
    public static final HashMap<Integer, String> taskStatsToTextMap = new HashMap<>();
    static {
        taskStatsToTextMap.put(TODO_STATUS, "To-Do");
        taskStatsToTextMap.put(IN_PROGRESS_STATUS, "In-Progress");
        taskStatsToTextMap.put(DONE_STATUS, "Done");

    }
}
