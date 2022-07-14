package com.example.wrk.fragments;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.example.wrk.CreateAdapter;
import com.example.wrk.R;
import com.example.wrk.ScratchCreateActivity;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.models.WorkoutTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CreateFragment extends Fragment {
    public static final String TAG = "CreateFragment";

    private FloatingActionButton fabCreate;
    private Transition.TransitionListener mEnterTransitionListener;
    private RecyclerView rvSavedWorkouts;
    protected CreateAdapter adapter;
    protected List<WorkoutTemplate> savedTemplates;

    public CreateFragment() {
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
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        savedTemplates = new ArrayList<>();
        fabCreate = view.findViewById(R.id.fabCreate);
        fabCreate.setVisibility(View.INVISIBLE);
        mEnterTransitionListener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) { }

            @Override
            public void onTransitionEnd(Transition transition) { enterReveal(view);}

            @Override
            public void onTransitionCancel(Transition transition) { }

            @Override
            public void onTransitionPause(Transition transition) { }

            @Override
            public void onTransitionResume(Transition transition) { }
        };
        Transition transition = new Transition() {
            @Override
            public void captureStartValues(TransitionValues transitionValues) { }

            @Override
            public void captureEndValues(TransitionValues transitionValues) { }
        };
        transition.addListener(mEnterTransitionListener);
        this.setEnterTransition(transition);

        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ScratchCreateActivity.class);
                startActivity(i);
            }
        });

        rvSavedWorkouts = view.findViewById(R.id.rvSavedWorkouts);
        adapter = new CreateAdapter(getContext(), savedTemplates);
        // link adapter and layout manager to RecyclerView
        rvSavedWorkouts.setAdapter(adapter);
        rvSavedWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));

        queryTemplates();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        queryTemplates();
        adapter.notifyDataSetChanged();
    }

    protected void queryTemplates() {
        ParseQuery<WorkoutTemplate> templatesQuery = ParseQuery.getQuery(WorkoutTemplate.class);
        // includes specified data
        templatesQuery.whereEqualTo(WorkoutTemplate.KEY_SAVED_BY, ParseUser.getCurrentUser());  // only templates saved by current user
        templatesQuery.include(WorkoutTemplate.KEY_TITLE);
        templatesQuery.include(WorkoutTemplate.KEY_COMPONENTS);
        templatesQuery.include(WorkoutTemplate.KEY_SAVED_BY);
        // order by creation date (newest first)
        templatesQuery.addDescendingOrder("createdAt");
        // async call for posts
        templatesQuery.findInBackground(new FindCallback<WorkoutTemplate>() {
            @Override
            public void done(List<WorkoutTemplate> template, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with fetching templates. ", e);
                    return;
                }
                // save received posts to list and notify adapter of new data
                savedTemplates.clear();
                savedTemplates.addAll(template);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void enterReveal(View view) {
        // previously invisible view
        final View myView = view.findViewById(R.id.fabCreate);

        // get the center for the clipping circle
        int cx = myView.getMeasuredWidth() / 2;
        int cy = myView.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;
        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        myView.setVisibility(View.VISIBLE);
        anim.start();
    }
}