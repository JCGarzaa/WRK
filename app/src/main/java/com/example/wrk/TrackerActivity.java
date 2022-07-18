package com.example.wrk;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

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
            @RequiresApi(api = Build.VERSION_CODES.O)
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
                updateUserStreak();
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
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateUserStreak() {
        Date lastWorkout = ParseUser.getCurrentUser().getDate("lastWorkout");
        int streak = ParseUser.getCurrentUser().getInt("streak");       // current daily streak
        int workoutsThisMonth = ParseUser.getCurrentUser().getInt("workoutsThisMonth");
        int lastWorkoutMonth = -1;      // initialize to -1 in case null
        long timeDifference = 0;

        final long SECONDS_IN_DAY = 86400;

        LocalDate today = LocalDate.now();
        long todayTime = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        // calculate difference in days since last workout
        if (lastWorkout != null) {
            LocalDate recentWorkout = lastWorkout.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long recentWorkoutTime = recentWorkout.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            lastWorkoutMonth = recentWorkout.getMonthValue();
            timeDifference = todayTime - recentWorkoutTime;
        }

        // increment streak only if there is a 1 day difference since the last workout.
        if (lastWorkout != null && timeDifference == SECONDS_IN_DAY) {
            streak++;           // increment streak by 1
        }
        // reset streak to 1 if more than 1 day has past since working out since completing workout
        else if (lastWorkout == null || timeDifference > SECONDS_IN_DAY){
            streak = 1;         // reset to 1 since completing the workout
        }
        // increment workoutsThisMonth if month has not changed
        if (lastWorkoutMonth == today.getMonthValue() && lastWorkoutMonth != -1) {
            workoutsThisMonth++;
        }
        else {
            workoutsThisMonth =  1;
        }

        // save to database
        ParseUser.getCurrentUser().put("streak", streak);
        ParseUser.getCurrentUser().put("workoutsThisMonth", workoutsThisMonth);
        ParseUser.getCurrentUser().put("lastWorkout", new Date());  // put date object of current time
        ParseUser.getCurrentUser().saveInBackground();
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