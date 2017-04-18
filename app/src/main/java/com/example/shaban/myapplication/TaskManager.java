package com.example.shaban.myapplication;

/**
 * Created by Sha'ban on 4/19/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class TaskManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "Task_Pref";

    // All Shared Preferences Keys
    private static final String TasksManager = "Task";

    public TaskManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void addTasks(String jsonObject) {
        editor.putString(TasksManager, jsonObject);
        editor.commit();
    }

    public JSONArray getTasks() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        String tasks = pref.getString(TasksManager, null);
        if(tasks == null){
            return jsonArray;
        }
        JSONObject jsonObject = new JSONObject(tasks);
        jsonArray = jsonObject.getJSONArray("Follows");
        return jsonArray;
    }

    public void clearTasks() {
        editor.clear();
        editor.commit();
    }
}