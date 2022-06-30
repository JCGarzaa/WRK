package com.example.wrk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.wrk.fragments.CreateExerciseDialogFragment;
import com.example.wrk.models.Exercise;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

// This activity shows a list of exercises to choose from that will be added to the template builder
public class ExerciseListActivity extends AppCompatActivity {
    public static final String TAG = "ExerciseListActivity";
    RecyclerView rvExercises;
    List<Exercise> exerciseList;
    ExerciseListAdapter adapter;
    FloatingActionButton fabCreateNewExercise;
    CreateExerciseDialogFragment createExerciseDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);
        rvExercises = findViewById(R.id.rvExercises);
        fabCreateNewExercise = findViewById(R.id.fabCreateNewExercise);
        exerciseList = new ArrayList<>();       // initialize empty list
        // link adapter and layout manager to RecyclerView
        adapter = new ExerciseListAdapter(this, exerciseList);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setAdapter(adapter);

        fabCreateNewExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExerciseDialog();
            }
        });
        queryExercises();
    }

    protected void queryExercises() {
        ParseQuery<Exercise> exerciseQuery = ParseQuery.getQuery(Exercise.class);
        // includes specified data
        exerciseQuery.include(Exercise.KEY_NAME);
        exerciseQuery.include(Exercise.KEY_BODYPART);
        // limits number of items to generate
        exerciseQuery.setLimit(10);
        // alphabetical order
        exerciseQuery.addAscendingOrder(Exercise.KEY_NAME);
        // async call for exercises
        exerciseQuery.findInBackground(new FindCallback<Exercise>() {
            @Override
            public void done(List<Exercise> workouts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with fetching exercises. ", e);
                    return;
                }
                // save received posts to list and notify adapter of new data
                exerciseList.addAll(workouts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showExerciseDialog() {
        createExerciseDialogFragment = new CreateExerciseDialogFragment();
        FragmentManager fm = getSupportFragmentManager();
        createExerciseDialogFragment.show(fm, "fragment_create_exercise_dialog");
    }
}