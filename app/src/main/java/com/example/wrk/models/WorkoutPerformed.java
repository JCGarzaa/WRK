package com.example.wrk.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

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

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public String getRelativeTimeAgo(String rawJsonDate) {
        String timeFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(timeFormat, Locale.ENGLISH);
        sf.setLenient(true);
        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
