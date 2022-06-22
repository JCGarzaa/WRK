package com.example.wrk.models;

import com.example.wrk.models.Exercise;
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("WorkoutComponent")
public class WorkoutComponent extends ParseObject {
    public static final String KEY_EXERCISE = "exercise";
    public static final String KEY_SETS = "sets";
    public static final String KEY_REPS = "reps";
    public static final String KEY_WEIGHT = "weight";

    public Exercise getExercise() { return (Exercise) getParseObject(KEY_EXERCISE); }
    public void setExercise(Exercise exercise) { put(KEY_EXERCISE, exercise); }

    public int getSets() { return getInt(KEY_SETS); }
    public void setSets(int sets) { put(KEY_SETS, sets); }

    public int getReps() { return getInt(KEY_REPS); }
    public void setReps(int reps) { put(KEY_REPS, reps); }

    public double getWeight() { return getDouble(KEY_WEIGHT); }
    public void setWeight(double weight) { put(KEY_WEIGHT, weight); }
}
