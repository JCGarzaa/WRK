package com.example.wrk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wrk.models.Exercise;

import java.util.List;

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ViewHolder> {
    Activity activity;
    List<Exercise> mExercises;

    public ExerciseListAdapter(Activity activity, List<Exercise> mExercises) {
        this.mExercises = mExercises;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(activity).inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = mExercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return mExercises.size();
    }

    public void clear() {
        mExercises.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExName;
        private TextView tvExBodyPart;
        private ImageButton ibAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExName = itemView.findViewById(R.id.tvExName);
            tvExBodyPart = itemView.findViewById(R.id.tvExBodyPart);
            ibAdd = itemView.findViewById(R.id.ibAdd);
        }

        public void bind(Exercise exercise) {
            tvExName.setText(exercise.getName());
            tvExBodyPart.setText(exercise.getBodyPart());
            ibAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, ScratchCreateActivity.class);
                    i.putExtra("exercise", exercise);
                    activity.setResult(Activity.RESULT_OK, i);
                    activity.finish();
                }
            });
        }
    }
}
