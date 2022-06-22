package com.example.wrk;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wrk.models.WorkoutComponent;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.models.WorkoutTemplate;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// This adapter is used to help populate the RecyclerView in the FeedFragment
public class WorkoutsAdapter extends RecyclerView.Adapter<WorkoutsAdapter.ViewHolder> {
    private Context mContext;
    private List<WorkoutPerformed> mWorkoutsPerformed;
    private List<WorkoutComponent> mWorkoutComponents;
    private List<WorkoutTemplate> mWorkoutTemplate;

    public WorkoutsAdapter(Context context, List<WorkoutPerformed> workoutsPerformed) {
        this.mContext = context;
        this.mWorkoutsPerformed = workoutsPerformed;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(mContext).inflate(R.layout.item_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutPerformed workout = mWorkoutsPerformed.get(position);
        try {
            holder.bind(workout);
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mWorkoutsPerformed.size();
    }

    public void clear() {
        mWorkoutsPerformed.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<WorkoutPerformed> list) {
        mWorkoutsPerformed.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private ImageView ivFeedPFP;
        private TableLayout tlWorkouts;
        private TextView tvWorkoutTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivFeedPFP = itemView.findViewById(R.id.ivFeedPFP);
            tlWorkouts = itemView.findViewById(R.id.tlWorkouts);
            tvWorkoutTitle = itemView.findViewById(R.id.tvWorkoutTitle);
        }

        public void bind(WorkoutPerformed workoutPerformed) throws ParseException, JSONException {
            Context tableContext = tlWorkouts.getContext();

            tvName.setText(workoutPerformed.getUser().getUsername());
            ParseFile image = workoutPerformed.getPFP();
            if (image != null) {
                Glide.with(mContext).load(workoutPerformed.getPFP().getUrl()).into(ivFeedPFP);
            }
            String title = workoutPerformed.getWorkout().getTitle();
            tvWorkoutTitle.setText(title);     // title of workout
            queryComponents(title);  // get components from the database

            // populate rows with exercise info from data gathered from query
            for (int i = 0; i < mWorkoutComponents.size(); i++) {
                WorkoutComponent component = mWorkoutComponents.get(i);
                String name = component.getExercise().getName();
                int sets = component.getSets();
                int reps = component.getReps();

                // create new row for each exercise
                TableRow tbrow = new TableRow(tableContext);
                // For each row, add 3 textviews for exercise name, sets, and reps
                TextView tvExercise = new TextView(tableContext);
                tvExercise.setText(name);
                tbrow.addView(tvExercise);
                TextView tvSets = new TextView(tableContext);
                tvSets.setText(String.valueOf(sets));
                tbrow.addView(tvSets);
                TextView tvReps = new TextView(tableContext);
                tvReps.setText(String.valueOf(reps));
                tbrow.addView(tvReps);

                tlWorkouts.addView(tbrow);      // link row to the tablelayout
            }
        }
    }

    private void queryComponents(String title) throws ParseException, JSONException {
        mWorkoutComponents = new ArrayList<>();     // initialize empty list each time a query is called

        ParseQuery<WorkoutTemplate> query = ParseQuery.getQuery(WorkoutTemplate.class);
        // includes specified data
        query.whereEqualTo(WorkoutTemplate.KEY_TITLE, title);   // sort with only those in the same workout
        query.include(WorkoutTemplate.KEY_COMPONENTS);
        mWorkoutTemplate = query.find();    // returns a list of one workoutTemplate
        ParseQuery<WorkoutComponent> query2 = ParseQuery.getQuery(WorkoutComponent.class);
        for (int i = 0; i < mWorkoutTemplate.get(0).getComponents().length(); i++) {
            // grab ID to ensure only those with same id get shown
            String id = mWorkoutTemplate.get(0).getComponents().getJSONObject(i).getString("objectId");
            // add filter to grab only components in the specific workout
            query2.whereEqualTo(WorkoutComponent.KEY_OBJECT_ID, id);
            query2.include(WorkoutComponent.KEY_EXERCISE);
            query2.include(WorkoutComponent.KEY_REPS);
            query2.include(WorkoutComponent.KEY_SETS);
            mWorkoutComponents.addAll(query2.find());  // add objects to list
        }
    }
}
