package com.example.wrk;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wrk.models.WorkoutComponent;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.models.WorkoutTemplate;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

// This adapter is used within the ProfileFragment recyclerview to show previous workouts
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    private Context mContext;
    private List<WorkoutPerformed> mWorkoutPerformed;
    private List<WorkoutComponent> mWorkoutComponents;

    public ProfileAdapter(Context context, List<WorkoutPerformed> workoutsPerformed) {
        this.mContext = context;
        this.mWorkoutPerformed = workoutsPerformed;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_workout, parent, false);
        return new ViewHolder(itemView, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutPerformed workout = mWorkoutPerformed.get(position);
        try {
            holder.bind(workout);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return mWorkoutPerformed.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View rootView;
        private TextView tvProfileWorkoutTitle;
        private TextView tvExerciseNames;

        public ViewHolder(View itemView, final Context context) {
            super(itemView);
            rootView = itemView;
            tvProfileWorkoutTitle = itemView.findViewById(R.id.tvProfileWorkoutTitle);
            tvExerciseNames = itemView.findViewById(R.id.tvExerciseNames);
        }

        public void bind(WorkoutPerformed workout) throws JSONException, ParseException {
            String title = workout.getWorkout().getTitle();
            tvProfileWorkoutTitle.setText(title);

            int numExercises = workout.getWorkout().getComponents().length();
            WorkoutTemplate template = workout.getWorkout();
            queryComponents(template);
            for (int i = 0; i < numExercises; i++) {
                tvExerciseNames.append(mWorkoutComponents.get(i).getExercise().getName());
                if (i + 1 < numExercises) {         // add commas for formatting
                    tvExerciseNames.append(", ");
                }
            }
        }
    }

    private void queryComponents(WorkoutTemplate template) throws ParseException, JSONException {
        mWorkoutComponents = new ArrayList<>();     // initialize empty list each time a query is called
        ParseQuery<WorkoutComponent> componentQuery = ParseQuery.getQuery(WorkoutComponent.class);
        for (int i = 0; i < template.getComponents().length(); i++) {
            // grab ID to ensure only those with same id get shown
            String id = template.getComponents().getJSONObject(i).getString("objectId");
            // add filter to grab only components in the specific workout
            componentQuery.whereEqualTo(WorkoutComponent.KEY_OBJECT_ID, id);
            componentQuery.include(WorkoutComponent.KEY_EXERCISE);
            componentQuery.include(WorkoutComponent.KEY_REPS);
            componentQuery.include(WorkoutComponent.KEY_SETS);
            mWorkoutComponents.addAll(componentQuery.find());  // add objects to list
        }
    }
}
