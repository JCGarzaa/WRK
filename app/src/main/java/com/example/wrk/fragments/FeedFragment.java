package com.example.wrk.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wrk.MainActivity;
import com.example.wrk.R;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.WorkoutsAdapter;
import com.example.wrk.models.WorkoutTemplate;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// FeedFragment is what is shown when the home logo is tapped within the app
public class FeedFragment extends Fragment {
    public static final String TAG = "FeedFragment";

    private RecyclerView rvWorkouts;
    protected List<WorkoutPerformed> workoutsPerformed;
    protected List<WorkoutPerformed> recentFollowedWorkouts;
    protected List<ParseUser> recentFollowedUsers;
    protected List<WorkoutTemplate> popularTemplates;
    protected List<WorkoutPerformed> mostPopularWorkouts;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // link views to the layout file
        rvWorkouts = view.findViewById(R.id.rvWorkoutsCompleted);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        workoutsPerformed = new ArrayList<>();
        adapter = new WorkoutsAdapter((MainActivity) getContext(), workoutsPerformed);
        rvWorkouts.setLayoutManager(layoutManager);
        rvWorkouts.setAdapter(adapter);
        try {
            queryRecentFollowedWorkouts();
        } catch (ParseException e) {
            Log.e(TAG, "ERROR FETCHING RECENT WORKOUTS. ", e);
        }
        try {
            queryPopularWorkouts();
        } catch (ParseException e) {
            Log.e(TAG, "ERROR FETCHING POPULAR WORKOUTS. ", e);
        }
        try {
            queryWorkouts();
        } catch (ParseException e) {
            Log.e(TAG, "ERROR FETCHING COMMUNITY WORKOUTS. ", e);
        }
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void queryRecentFollowedWorkouts() throws ParseException {
        // query most recent posts within past two days
        final long DAYS_COUNT = 2;
        LocalDate today = LocalDate.now();

        // get week before current date
        LocalDate daysAgoDate = today.minusDays(DAYS_COUNT);

        ParseQuery<WorkoutPerformed> performedQuery = ParseQuery.getQuery(WorkoutPerformed.class);
        performedQuery.include(WorkoutPerformed.KEY_USER);
        performedQuery.include(WorkoutPerformed.KEY_WORKOUT);
        performedQuery.addDescendingOrder(WorkoutPerformed.KEY_CREATED_AT);
        performedQuery.setLimit(5);
        // specify workouts from the user's following list
        performedQuery.whereContainedIn(WorkoutPerformed.KEY_USER, ParseUser.getCurrentUser().getList("following"));

        List<WorkoutPerformed> list = new ArrayList<>();
        recentFollowedWorkouts = new ArrayList<>();
        recentFollowedUsers = new ArrayList<>();
        list.addAll(performedQuery.find());
        for (int i = 0; i < list.size(); i++) {
            LocalDate postDate = list.get(i)
                    .getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            // only add the posts within the past 2 days
            if (postDate.isAfter(daysAgoDate)) {
                recentFollowedWorkouts.add(list.get(i));
                recentFollowedUsers.add(list.get(i).getUser());
            }
        }
        workoutsPerformed.addAll(recentFollowedWorkouts);
    }

    protected void queryPopularWorkouts() throws ParseException {
        ParseQuery<WorkoutTemplate> performedQuery = ParseQuery.getQuery(WorkoutTemplate.class);
        // includes specified data
        performedQuery.include(WorkoutTemplate.KEY_TITLE);
        performedQuery.include(WorkoutTemplate.KEY_COMPONENTS);
        performedQuery.addDescendingOrder(WorkoutTemplate.KEY_SAVED_BY);
        List<WorkoutTemplate> allTemplates = new ArrayList<>();
        allTemplates.addAll(performedQuery.find());

        popularTemplates = new ArrayList<>();
        mostPopularWorkouts = new ArrayList<>();

        // compare amount of users that saved the templates
        Comparator<WorkoutTemplate> workoutTemplateComparator = new Comparator<WorkoutTemplate>() {
            @Override
            public int compare(WorkoutTemplate t1, WorkoutTemplate t2) {
                return Integer.compare(t1.getSavedBy().size(), t2.getSavedBy().size());
            }
        };
        // sort templates by greatest to least in save count
        Collections.sort(allTemplates, workoutTemplateComparator);
        Collections.reverse(allTemplates);
        // place up to top 3 templates into the popular ones
        for (int i = 0; i < allTemplates.size(); i++) {
            if (i == 3) {
                break;
            }
            else {
                popularTemplates.add(allTemplates.get(i));
            }
        }
        ParseQuery<WorkoutPerformed> parseQuery = ParseQuery.getQuery(WorkoutPerformed.class);
        parseQuery.include(WorkoutPerformed.KEY_USER);
        parseQuery.include(WorkoutPerformed.KEY_WORKOUT);
        // look for posts that are within the popular templates
        parseQuery.whereContainedIn(WorkoutPerformed.KEY_WORKOUT, popularTemplates);
        // newest first
        parseQuery.addDescendingOrder(WorkoutPerformed.KEY_CREATED_AT);
        mostPopularWorkouts.addAll(parseQuery.find());

        // remove if template is the same as another post
        for (int i = 0; i < mostPopularWorkouts.size(); i++) {
            for (int j = i + 1; j < mostPopularWorkouts.size(); j++) {
                if (mostPopularWorkouts.get(i).getWorkout().hasSameId(mostPopularWorkouts.get(j).getWorkout())) {
                    mostPopularWorkouts.remove(j);
                    j = i;
                }
            }
        }
        workoutsPerformed.addAll(mostPopularWorkouts);
    }

    // query community workouts, mix of older posts from friends and less popular templates
    protected void queryWorkouts() throws ParseException {
        ParseQuery<WorkoutPerformed> performedQuery = ParseQuery.getQuery(WorkoutPerformed.class);
        // includes specified data
        performedQuery.include(WorkoutPerformed.KEY_USER);
        performedQuery.include(WorkoutPerformed.KEY_WORKOUT);
        // to remove posts that have already been added
        performedQuery.whereNotContainedIn(WorkoutPerformed.KEY_USER, recentFollowedUsers);
        performedQuery.whereNotContainedIn(WorkoutPerformed.KEY_WORKOUT, popularTemplates);
        // limits number of items to generate
        performedQuery.setLimit(4);
        // order by creation date (newest first)
        performedQuery.addDescendingOrder("createdAt");
        // async call for posts
        workoutsPerformed.addAll(performedQuery.find());
    }
}