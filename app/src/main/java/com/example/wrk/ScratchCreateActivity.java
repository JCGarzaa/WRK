package com.example.wrk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.wrk.models.Exercise;
import com.example.wrk.models.WorkoutComponent;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.models.WorkoutTemplate;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

// activity is used for creating a workout from scratch
public class ScratchCreateActivity extends AppCompatActivity {
    EditText etCreateWorkoutTitle;
    ImageButton ibDeleteWorkout;
    ImageButton ibAddExercise;
    Button btnSave;
    ArrayList<WorkoutComponent> mComponents;
    RecyclerView rvComponents;
    ScratchCreateAdapter adapter;
    WorkoutTemplate workoutTemplate;
    List<WorkoutPerformed> workoutsPerformed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scratch_create);

        etCreateWorkoutTitle = findViewById(R.id.etCreateWorkoutTitle);
        ibDeleteWorkout = findViewById(R.id.ibDeleteWorkout);
        ibAddExercise = findViewById(R.id.ibAddExercise);
        btnSave = findViewById(R.id.btnSave);
        rvComponents = findViewById(R.id.rvCreateComponents);
        mComponents = new ArrayList<>();
        workoutsPerformed = new ArrayList<>();

        workoutTemplate = Parcels.unwrap(getIntent().getParcelableExtra("template"));     // receive existing template after edit button clicked
        if (workoutTemplate != null) { // if user clicks to edit existing workout, template will not be null
            try {
                queryComponents(workoutTemplate);
                etCreateWorkoutTitle.setText(workoutTemplate.getTitle());               // set title to existing workout title
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
        adapter = new ScratchCreateAdapter(this, mComponents);
        rvComponents.setAdapter(adapter);
        rvComponents.setLayoutManager(new LinearLayoutManager(this));

        ibAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ScratchCreateActivity.this, ExerciseListActivity.class);
                startActivityForResult(i, 1);
            }
        });

        ibDeleteWorkout.setOnClickListener(new View.OnClickListener() {
            // Create an alert dialog to confirm deletion
            @Override
            public void onClick(View v) {
                queryWorkouts();
                AlertDialog.Builder builder = new AlertDialog.Builder(ScratchCreateActivity.this);
                AlertDialog dialog;
                builder.setTitle("Delete Template");
                builder.setMessage("Are you sure you want to delete this template?");
                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (workoutTemplate != null) {
                            try {
                                // cannot delete template if others have posted with this workout
                                for (int i = 0; i < workoutsPerformed.size(); i++) {
                                    if (workoutsPerformed.get(i).getWorkout().getObjectId().equals(workoutTemplate.getObjectId())) {
                                        Toast.makeText(ScratchCreateActivity.this, "Deleting this template will interfere with other's posts.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                workoutTemplate.delete();       // remove template from database
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dial) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                    }
                });
                dialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etCreateWorkoutTitle.getText().toString();
                if (title != null && !title.isEmpty()) {
                    if (mComponents.size() != 0) {
                        try {
                            if (workoutTemplate != null) {
                                updateTemplate(workoutTemplate, title, mComponents);  // update template in database
                            } else {
                                saveNewTemplate(title, mComponents);       // save template to database
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        finish();       // go back to create fragment
                    }
                    else {
                        Toast.makeText(ScratchCreateActivity.this, "No exercises found.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(ScratchCreateActivity.this, "Please enter a title.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Exercise exercise = data.getParcelableExtra("exercise");
                WorkoutComponent component = new WorkoutComponent();
                component.setExercise(exercise);
                component.setSets(1);
                mComponents.add(component);
                adapter.notifyItemInserted(mComponents.size());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateTemplate(WorkoutTemplate workoutTemplate, String title, ArrayList<WorkoutComponent> components) throws ParseException {
        workoutTemplate.setTitle(title);                // for if title has been changed
        workoutTemplate.setComponents(components);      // for if components changed
        workoutTemplate.saveUserTemplate();             // check if user is already in savedBy list in database
        workoutTemplate.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(ScratchCreateActivity.this, "Error while updating template", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveNewTemplate(String title, ArrayList<WorkoutComponent> components) throws ParseException {
        List<ParseUser> savedBy = new ArrayList<>();
        savedBy.add(ParseUser.getCurrentUser());
        WorkoutTemplate template = new WorkoutTemplate();
        template.setTitle(title);
        template.setComponents(components);
        template.setSavedBy(savedBy);
        template.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(ScratchCreateActivity.this, "Error while saving template", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void queryComponents(WorkoutTemplate template) throws ParseException, JSONException {
        mComponents = new ArrayList<>();     // initialize empty list each time a query is called
        JSONArray componentArray = template.getComponents();
        ParseQuery<WorkoutComponent> componentQuery = ParseQuery.getQuery(WorkoutComponent.class);
        for (int i = 0; i < componentArray.length(); i++) {
            // grab ID to ensure only those with same id get shown
            String id = componentArray.getJSONObject(i).getString("objectId");
            // add filter to grab only components in the specific workout
            componentQuery.whereEqualTo(WorkoutComponent.KEY_OBJECT_ID, id);
            componentQuery.include(WorkoutComponent.KEY_EXERCISE);
            componentQuery.include(WorkoutComponent.KEY_REPS);
            componentQuery.include(WorkoutComponent.KEY_SETS);
            mComponents.addAll(componentQuery.find());  // add objects to list
        }
    }

    private void queryWorkouts() {
        workoutsPerformed.clear();
        ParseQuery<WorkoutPerformed> performedQuery = ParseQuery.getQuery(WorkoutPerformed.class);
        // includes specified data
        performedQuery.include(WorkoutPerformed.KEY_USER);
        performedQuery.include(WorkoutPerformed.KEY_WORKOUT);
        // order by creation date (newest first)
        performedQuery.addDescendingOrder("createdAt");
        // async call for posts
        performedQuery.findInBackground(new FindCallback<WorkoutPerformed>() {
            @Override
            public void done(List<WorkoutPerformed> workouts, ParseException e) {
                if (e != null) {
                    Log.e("ScratchCreateActivity", "Error with fetching workouts. ", e);
                    return;
                }
                // save received posts to list
                workoutsPerformed.addAll(workouts);
            }
        });
    }
}