package com.example.wrk.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.wrk.ExerciseListActivity;
import com.example.wrk.R;
import com.example.wrk.models.Exercise;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class CreateExerciseDialogFragment extends DialogFragment {

    private EditText etCreateExerciseName;
    private Button btnCreateExercise;
    private String selectedBodyPart;
    private RadioGroup radioGroup;
    private List<Exercise> exerciseList;
    private ExerciseListActivity activity;

    public CreateExerciseDialogFragment() {
        // Required empty public constructor
    }

    public CreateExerciseDialogFragment(ExerciseListActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_exercise_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exerciseList = new ArrayList<>();
        queryExercises();
        etCreateExerciseName = view.findViewById(R.id.etCreateExerciseName);
        btnCreateExercise = view.findViewById(R.id.btnCreateExercise);
        radioGroup = view.findViewById(R.id.radioGroup);
        btnCreateExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save new exercise to database
                String exerciseName = etCreateExerciseName.getText().toString();
                Exercise exercise = new Exercise();
                if (!exerciseName.isEmpty()) {
                    exercise.setName(exerciseName);
                    if (selectedBodyPart != null && !selectedBodyPart.isEmpty()) {
                        // prevent duplicates from being created
                        for (int i = 0; i < exerciseList.size(); i++) {
                            if (exerciseList.get(i).getName().equals(exerciseName)) {
                                Toast.makeText(getContext(), "Exercise already exists.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        exercise.setBodyPart(selectedBodyPart);
                        try {
                            exercise.save();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        activity.adapter.clear();
                        activity.queryExercises();
                        activity.adapter.notifyDataSetChanged();
                        dismiss();

                    }
                    else {
                        Toast.makeText(getContext(), "Please select a body part.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), "Please enter a name.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // assign selectedBodyPart based on button selected
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.radioArms:
                        selectedBodyPart = getString(R.string.arms);
                        break;
                    case R.id.radioBack:
                        selectedBodyPart = getString(R.string.back);
                        break;
                    case R.id.radioCore:
                        selectedBodyPart = getString(R.string.core);
                        break;
                    case R.id.radioChest:
                        selectedBodyPart = getString(R.string.chest);
                        break;
                    case R.id.radioCalves:
                        selectedBodyPart = getString(R.string.calves);
                        break;
                    case R.id.radioLegs:
                        selectedBodyPart = getString(R.string.legs);
                        break;
                    case R.id.radioShoulders:
                        selectedBodyPart = getString(R.string.shoulders);
                        break;
                    case R.id.radioCardio:
                        selectedBodyPart = getString(R.string.cardio);
                        break;
                    case R.id.radioFullBody:
                        selectedBodyPart = getString(R.string.fullbody);
                        break;
                    case R.id.radioCalisthenics:
                        selectedBodyPart = getString(R.string.calisthenics);
                        break;
                }
            }
        });
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
                    return;
                }
                // save received posts to list and notify adapter of new data
                exerciseList.addAll(workouts);
            }
        });
    }
}