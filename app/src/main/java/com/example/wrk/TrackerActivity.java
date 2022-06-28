package com.example.wrk;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.wrk.fragments.CreateFragment;
import com.example.wrk.models.Exercise;
import com.example.wrk.models.WorkoutComponent;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.models.WorkoutTemplate;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;

public class TrackerActivity extends AppCompatActivity {
    EditText etTrackerWorkoutTitle;
    Button btnFinish;
    ImageButton ibADDExercise;
    WorkoutTemplate workoutTemplate;
    RecyclerView rvTrackerComponents;
    ScratchCreateAdapter adapter;
    ArrayList<WorkoutComponent> mComponents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        etTrackerWorkoutTitle = findViewById(R.id.etTrackerWorkoutTitle);
        btnFinish = findViewById(R.id.btnFinish);
        ibADDExercise = findViewById(R.id.ibADDExercise);
        rvTrackerComponents = findViewById(R.id.rvTrackerComponents);

        workoutTemplate = Parcels.unwrap(getIntent().getParcelableExtra("template"));
        try {
            queryComponents(workoutTemplate);
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
        etTrackerWorkoutTitle.setText(workoutTemplate.getTitle());

        ibADDExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrackerActivity.this, ExerciseListActivity.class);
                startActivityForResult(i, 1);
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = etTrackerWorkoutTitle.getText().toString();
                if (title != null && !title.isEmpty()) {
                    if (mComponents.size() != 0) {
                        try {
                            updateTemplate(workoutTemplate, title, mComponents);  // update template in database
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(TrackerActivity.this, "No exercises found.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(TrackerActivity.this, "Please enter a title.", Toast.LENGTH_SHORT).show();
                }
                publishWorkout();           // save to database and post to feed
                Intent i = new Intent(TrackerActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        adapter = new ScratchCreateAdapter(this, mComponents);
        rvTrackerComponents.setLayoutManager(new LinearLayoutManager(this));
        rvTrackerComponents.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Exercise exercise = (Exercise) data.getParcelableExtra("exercise");
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
        workoutTemplate.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(TrackerActivity.this, "Error while updating template", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void publishWorkout() {
        WorkoutPerformed workoutPerformed = new WorkoutPerformed();
        workoutPerformed.setWorkout(workoutTemplate);
        workoutPerformed.setUser(ParseUser.getCurrentUser());
        workoutPerformed.setStatus(true);       // visibility to public
        workoutPerformed.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(TrackerActivity.this, "Error while publishing to feed.", Toast.LENGTH_SHORT).show();
                    return;
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
}