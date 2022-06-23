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

import com.example.wrk.CreateAdapter;
import com.example.wrk.R;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.models.WorkoutTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class CreateFragment extends Fragment {
    public static final String TAG = "CreateFragment";

    private FloatingActionButton fabCreate;
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

        rvSavedWorkouts = view.findViewById(R.id.rvSavedWorkouts);
        adapter = new CreateAdapter(getContext(), savedTemplates);
        // link adapter and layout manager to RecyclerView
        rvSavedWorkouts.setAdapter(adapter);
        rvSavedWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        queryTemplates();
    }

    protected void queryTemplates() {
        ParseQuery<WorkoutTemplate> templatesQuery = ParseQuery.getQuery(WorkoutTemplate.class);
        // includes specified data
        templatesQuery.include(WorkoutTemplate.KEY_TITLE);
        templatesQuery.include(WorkoutTemplate.KEY_COMPONENTS);
        // limits number of items to generate
        templatesQuery.setLimit(6);
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
                savedTemplates.addAll(template);
                adapter.notifyDataSetChanged();
            }
        });
    }
}