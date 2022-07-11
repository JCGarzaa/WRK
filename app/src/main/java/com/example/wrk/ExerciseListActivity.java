package com.example.wrk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

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
    private RecyclerView rvExercises;
    private List<Exercise> exerciseList;
    public ExerciseListAdapter adapter;
    private FloatingActionButton fabCreateNewExercise;
    SearchView searchView;
    private CreateExerciseDialogFragment createExerciseDialogFragment;

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
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchQuery) {
                if (searchQuery.isEmpty()) {
                    queryAllExercises();
                }
                else {
                    querySearchExercises(searchQuery);      // filter exercises
                }
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                querySearchExercises(searchQuery);
                return false;
            }
        });
        queryAllExercises();
    }

    public void queryAllExercises() {
        ParseQuery<Exercise> exerciseQuery = ParseQuery.getQuery(Exercise.class);
        // includes specified data
        exerciseQuery.include(Exercise.KEY_NAME);
        exerciseQuery.include(Exercise.KEY_BODYPART);
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
                exerciseList.clear();
                exerciseList.addAll(workouts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    protected void querySearchExercises(String searchQuery) {
        ParseQuery<Exercise> exerciseQuery = ParseQuery.getQuery(Exercise.class);
        exerciseQuery.include(Exercise.KEY_NAME);
        exerciseQuery.include(Exercise.KEY_BODYPART);
        // query for exercises with name similar to entry
        exerciseQuery.whereMatches(Exercise.KEY_NAME, searchQuery, "i");
        // alphabetical order
        exerciseQuery.addAscendingOrder(Exercise.KEY_NAME);
        // async call for exercises
        exerciseQuery.findInBackground(new FindCallback<Exercise>() {
            @Override
            public void done(List<Exercise> exercises, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with searching for exercises. ", e);
                    return;
                }
                // save received posts to list and notify adapter of new data
                exerciseList.clear();
                exerciseList.addAll(exercises);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showExerciseDialog() {
        createExerciseDialogFragment = new CreateExerciseDialogFragment(this);
        FragmentManager fm = getSupportFragmentManager();
        createExerciseDialogFragment.show(fm, "fragment_create_exercise_dialog");
    }
}
