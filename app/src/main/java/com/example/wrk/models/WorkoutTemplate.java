package com.example.wrk.models;

import com.example.wrk.models.WorkoutComponent;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

@ParseClassName("WorkoutTemplates")
public class WorkoutTemplate extends ParseObject {
    public static final String KEY_TITLE = "title";
    public static final String KEY_COMPONENTS = "components";

    public String getTitle() { return getString(KEY_TITLE); }
    public void setTitle(String title) { put(KEY_TITLE, title); }

    public JSONArray getComponents() { return getJSONArray(KEY_COMPONENTS); }
    public void setComponents(ArrayList<WorkoutComponent> components) {
        put(KEY_COMPONENTS, components);
    }

}
