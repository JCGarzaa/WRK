package com.example.wrk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wrk.fragments.ProfileFragment;
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
    private MainActivity mainActivity;
    private List<WorkoutPerformed> mWorkoutsPerformed;
    private List<WorkoutComponent> mWorkoutComponents;
    private List<WorkoutTemplate> mWorkoutTemplate;

    public WorkoutsAdapter(MainActivity activity, List<WorkoutPerformed> workoutsPerformed) {
        this.mainActivity = activity;
        this.mWorkoutsPerformed = workoutsPerformed;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(mainActivity).inflate(R.layout.item_workout, parent, false);
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
        private ImageView ivBicep;
        private AnimatedVectorDrawableCompat avd;
        private AnimatedVectorDrawable avd2;
        private TextView tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivFeedPFP = itemView.findViewById(R.id.ivFeedPFP);
            tlWorkouts = itemView.findViewById(R.id.tlWorkouts);
            tvWorkoutTitle = itemView.findViewById(R.id.tvWorkoutTitle);
            ivBicep = itemView.findViewById(R.id.ivBicep);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void bind(WorkoutPerformed workoutPerformed) throws ParseException, JSONException {
            Context tableContext = tlWorkouts.getContext();

            tvName.setText(workoutPerformed.getUser().getUsername());
            String rawTime = workoutPerformed.getCreatedAt().toString();
            tvTimestamp.setText(workoutPerformed.getRelativeTimeAgo(rawTime));
            ParseFile image = workoutPerformed.getPFP();
            if (image != null) {
                Glide.with(mainActivity)
                        .load(workoutPerformed.getPFP().getUrl())
                        .transform(new RoundedCorners(150))
                        .into(ivFeedPFP);
            }
            else {
                // upload a default picture
                Glide.with(mainActivity)
                        .load(R.drawable.account)
                        .centerCrop()
                        .transform(new RoundedCorners(150))
                        .into(ivFeedPFP);
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
                tvExercise.setTextColor(mainActivity.getResources().getColor(R.color.red_light));
                tvExercise.setText(name);
                tbrow.addView(tvExercise);
                TextView tvSets = new TextView(tableContext);
                tvSets.setTextColor(mainActivity.getResources().getColor(R.color.red_light));
                tvSets.setText(String.valueOf(sets));
                tbrow.addView(tvSets);
                TextView tvReps = new TextView(tableContext);
                tvReps.setTextColor(mainActivity.getResources().getColor(R.color.red_light));
                tvReps.setText(String.valueOf(reps));
                tbrow.addView(tvReps);

                tlWorkouts.addView(tbrow);      // link row to the tablelayout
            }
            // double tap action to save a template
            tlWorkouts.setOnTouchListener(new OnDoubleTapListener(mainActivity) {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDoubleTap(MotionEvent e) {
                    // save template to users account if not already saved
                    WorkoutTemplate tappedTemplate = workoutPerformed.getWorkout();
                    if (!tappedTemplate.isSavedByCurrentUser()) {
                        tappedTemplate.saveUserTemplate();
                        final Drawable drawable = ivBicep.getDrawable();
                        ivBicep.setAlpha(0.7f);
                        if (drawable instanceof AnimatedVectorDrawableCompat) {
                            avd = (AnimatedVectorDrawableCompat) drawable;
                            avd.start();    // start animation
                        }
                        // for other devices
                        else if (drawable instanceof AnimatedVectorDrawable) {
                            avd2 = (AnimatedVectorDrawable) drawable;
                            avd2.start();
                        }
                    }
                    else {
                        Toast.makeText(mainActivity, "You already have this template saved.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // view other people's profile by clicking on their profile picture
            ivFeedPFP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser user = workoutPerformed.getUser();
                    final FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                    Fragment fragment = new ProfileFragment(mainActivity, user);
                    fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                }
            });
        }
    }

    private void queryComponents(String title) throws ParseException, JSONException {
        mWorkoutComponents = new ArrayList<>();     // initialize empty list each time a query is called

        ParseQuery<WorkoutTemplate> templateQuery = ParseQuery.getQuery(WorkoutTemplate.class);
        // includes specified data
        templateQuery.whereEqualTo(WorkoutTemplate.KEY_TITLE, title);   // sort with only those in the same workout
        templateQuery.include(WorkoutTemplate.KEY_COMPONENTS);
        mWorkoutTemplate = templateQuery.find();    // returns a list of one workoutTemplate
        ParseQuery<WorkoutComponent> componentQuery = ParseQuery.getQuery(WorkoutComponent.class);
        for (int i = 0; i < mWorkoutTemplate.get(0).getComponents().length(); i++) {
            // grab ID to ensure only those with same id get shown
            String id = mWorkoutTemplate.get(0).getComponents().getJSONObject(i).getString("objectId");
            // add filter to grab only components in the specific workout
            componentQuery.whereEqualTo(WorkoutComponent.KEY_OBJECT_ID, id);
            componentQuery.include(WorkoutComponent.KEY_EXERCISE);
            componentQuery.include(WorkoutComponent.KEY_REPS);
            componentQuery.include(WorkoutComponent.KEY_SETS);
            mWorkoutComponents.addAll(componentQuery.find());  // add objects to list
        }
    }
}
