package com.example.wrk.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wrk.R;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.WorkoutsAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {
    public static final String TAG = "FeedFragment";

    private RecyclerView rvWorkouts;
    protected List<WorkoutPerformed> workoutsPerformed;
    protected  WorkoutsAdapter adapter;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvWorkouts = view.findViewById(R.id.rvWorkoutsCompleted);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        workoutsPerformed = new ArrayList<>();
        adapter = new WorkoutsAdapter(getContext(), workoutsPerformed);
        rvWorkouts.setLayoutManager(layoutManager);
        rvWorkouts.setAdapter(adapter);

        queryWorkouts();
    }

    protected void queryWorkouts() {
        ParseQuery<WorkoutPerformed> query = ParseQuery.getQuery(WorkoutPerformed.class);
        // includes specified data
        query.include(WorkoutPerformed.KEY_USER);
        query.include(WorkoutPerformed.KEY_WORKOUT);
        // limits number of items to generate
        query.setLimit(5);
        // order by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // async call for posts
        query.findInBackground(new FindCallback<WorkoutPerformed>() {
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
}