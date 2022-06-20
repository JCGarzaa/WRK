package com.example.wrk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wrk.models.WorkoutPerformed;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class WorkoutsAdapter extends RecyclerView.Adapter<WorkoutsAdapter.ViewHolder> {
    private Context mContext;
    private List<WorkoutPerformed> mWorkoutsPerformed;

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
        holder.bind(workout);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivFeedPFP = itemView.findViewById(R.id.ivFeedPFP);

        }

        public void bind(WorkoutPerformed workoutPerformed) {
            tvName.setText(workoutPerformed.getUser().getUsername());
            ParseFile image = workoutPerformed.getPFP();
            if (image != null) {
                Glide.with(mContext).load(workoutPerformed.getPFP().getUrl()).into(ivFeedPFP);
            }
        }
    }
}
