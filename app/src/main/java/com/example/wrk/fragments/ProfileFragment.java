package com.example.wrk.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wrk.GymMapsActivity;
import com.example.wrk.MainActivity;
import com.example.wrk.ProfileAdapter;
import com.example.wrk.R;
import com.example.wrk.models.WorkoutPerformed;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileFragment extends Fragment {
    public static final String TAG = "ProfileFragment";

    private MainActivity mainActivity;
    private ParseUser user;
    private ImageView ivProfilePicture;
    private Button btnLogout;
    private TextView tvProfileName;
    private TextView tvProfileUsername;
    private TextView tvDailyStreak;
    private ImageButton ibGymsNearMe;
    private RecyclerView rvPrevWorkouts;
    protected ProfileAdapter adapter;
    protected List<WorkoutPerformed> workoutsPerformed;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public ProfileFragment(MainActivity mainActivity, ParseUser user) {
        this.mainActivity = mainActivity;
        this.user = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);

        ParseFile file = user.getParseFile("profilePic");
        // check if there is a profile picture that the user has
        if (file != null) {
            Glide.with(getContext())
                    .load(file.getUrl())
                    .centerCrop()
                    .transform(new RoundedCorners(150))
                    .into(ivProfilePicture);
        }
        else {
            // upload a default picture
            Glide.with(getContext())
                    .load(R.drawable.instagram_user_outline_24)
                    .centerCrop()
                    .transform(new RoundedCorners(150))
                    .into(ivProfilePicture);
        }

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileName.setText(user.getString("name"));

        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileUsername.setText(user.getUsername());

        updateStreak(user);     // check each time someone visits a profile
        tvDailyStreak = view.findViewById(R.id.tvDailyStreak);
        tvDailyStreak.setText(String.valueOf(user.getInt("streak")));

        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                mainActivity.goLogin();
            }
        });

        ibGymsNearMe = view.findViewById(R.id.ibGymsNearMe);
        ibGymsNearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), GymMapsActivity.class);
                startActivity(i);
            }
        });
        rvPrevWorkouts = view.findViewById(R.id.rvPosts);
        rvPrevWorkouts.setHasFixedSize(true);      // allows for optimizations
        workoutsPerformed = new ArrayList<>();
        // 2 column grid layout
        final GridLayoutManager layout = new GridLayoutManager(getContext(), 2);
        rvPrevWorkouts.setLayoutManager(layout);
        adapter = new ProfileAdapter(getContext(), workoutsPerformed);
        rvPrevWorkouts.setAdapter(adapter);
        queryWorkouts();
    }

    protected void queryWorkouts() {
        ParseQuery<WorkoutPerformed> performedQuery = ParseQuery.getQuery(WorkoutPerformed.class);
        // includes specified data
        performedQuery.whereEqualTo(WorkoutPerformed.KEY_USER, user);
        performedQuery.include(WorkoutPerformed.KEY_USER);
        performedQuery.include(WorkoutPerformed.KEY_WORKOUT);
        // limits number of items to generate
        performedQuery.setLimit(6);
        // order by creation date (newest first)
        performedQuery.addDescendingOrder("createdAt");
        // async call for posts
        performedQuery.findInBackground(new FindCallback<WorkoutPerformed>() {
            @Override
            public void done(List<WorkoutPerformed> workouts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with fetching workouts. ", e);
                    return;
                }
                // save received posts to list and notify adapter of new data
                workoutsPerformed.addAll(workouts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void updateStreak(ParseUser user) {
        Date lastWorkout = user.getDate("lastWorkout");
        int streak = user.getInt("streak");     // current daily streak
        long timeDifference = 0;
        final long SECONDS_IN_DAY = 86400;

        if (lastWorkout != null) {
            // calculate difference in days since last workout
            LocalDate today = LocalDate.now();
            long todayTime = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

            LocalDate recentWorkout = lastWorkout.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long recentWorkoutTime = recentWorkout.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

            timeDifference = todayTime - recentWorkoutTime;
        }

        // reset streak to 0 if more than 1 day has past since working out
        if (lastWorkout == null || timeDifference > SECONDS_IN_DAY) {
            streak = 0;
        }

        // save to database
        user.put("streak", streak);
        user.saveInBackground();
    }
}