package com.example.wrk;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.wrk.models.WorkoutComponent;
import com.example.wrk.models.WorkoutTemplate;
import com.parse.ParseException;
import com.parse.SaveCallback;

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
        adapter = new ScratchCreateAdapter(this, mComponents);
        rvComponents.setAdapter(adapter);
        rvComponents.setLayoutManager(new LinearLayoutManager(this));

        ibAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ScratchCreateActivity.this, ExerciseListActivity.class);
                startActivity(i);
            }
        });

        ibDeleteWorkout.setOnClickListener(new View.OnClickListener() {
            // Create an alert dialog to confirm deletion
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScratchCreateActivity.this);
                AlertDialog dialog;
                builder.setTitle("Delete Template");
                builder.setMessage("Are you sure you want to delete this template?");
                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                if (!title.isEmpty()) {
                    if (mComponents.size() != 0) {
                        saveTemplate(title, mComponents);       // save template to database
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

    private void saveTemplate(String title, ArrayList<WorkoutComponent> components) {
        WorkoutTemplate template = new WorkoutTemplate();
        template.setTitle(title);
        template.setComponents(components);
        template.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(ScratchCreateActivity.this, "Error while saving template", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}