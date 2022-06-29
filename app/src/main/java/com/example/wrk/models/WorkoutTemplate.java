package com.example.wrk.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.wrk.models.WorkoutComponent;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("WorkoutTemplates")
public class WorkoutTemplate extends ParseObject {
    public static final String KEY_TITLE = "title";
    public static final String KEY_COMPONENTS = "components";
    public static final String KEY_SAVED_BY = "savedBy";

    public WorkoutTemplate() { } // required empty constructor for parceler

    public String getTitle() { return getString(KEY_TITLE); }
    public void setTitle(String title) { put(KEY_TITLE, title); }

    public JSONArray getComponents() { return getJSONArray(KEY_COMPONENTS); }
    public void setComponents(ArrayList<WorkoutComponent> components) {
        put(KEY_COMPONENTS, components);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean isSavedByCurrentUser() {
        List <ParseUser> savedBy = getSavedBy();
        for (int i = 0; i< savedBy.size(); i++) {
            if (savedBy.get(i).hasSameId(ParseUser.getCurrentUser())) {
                return true;
            }
        }
        return false;
    }

    public List<ParseUser> getSavedBy() {
        List<ParseUser> likedBy = getList(KEY_SAVED_BY);
        if (likedBy == null) {
            return new ArrayList<>();
        }
        return getList(KEY_SAVED_BY);
    }
    public void setSavedBy(List<ParseUser> newSavedBy) { put(KEY_SAVED_BY, newSavedBy); }

    public void unsaveTemplate() {
        List<ParseUser> savedBy = getSavedBy();
        for (int i = 0; i < savedBy.size(); i++) {
            if (savedBy.get(i).hasSameId(ParseUser.getCurrentUser())) {
                savedBy.remove(i);
            }
        }
        setSavedBy(savedBy);
        saveInBackground();
    }

    public void saveUserTemplate() {
        unsaveTemplate();
        List<ParseUser> savedBy = getSavedBy();
        savedBy.add(ParseUser.getCurrentUser());
        setSavedBy(savedBy);
        saveInBackground();
    }
}
