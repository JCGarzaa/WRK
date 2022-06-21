package com.example.wrk.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("WorkoutPerformed")
public class WorkoutPerformed extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_WORKOUT = "workout";
    public static final String KEY_ISPUBLIC = "isPublic";
    public static final String KEY_PFP = "profilePic";

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }
    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setStatus(boolean status) { put(KEY_ISPUBLIC, status); }
    public boolean getStatus() { return getBoolean(KEY_ISPUBLIC); }

    public ParseFile getPFP() { return getUser().getParseFile(KEY_PFP); }

    public WorkoutTemplate getWorkout() { return (WorkoutTemplate) getParseObject(KEY_WORKOUT); }
    public void setWorkout(WorkoutTemplate workout) { put(KEY_WORKOUT, workout); }

}
