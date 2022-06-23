package com.example.wrk;

import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateAdapter extends RecyclerView.Adapter<CreateAdapter.ViewHolder> {
    Context mContext;
    List<WorkoutTemplate> workoutTemplates;
    List<WorkoutComponent> workoutComponents;
    List<String> bodyParts;

    public CreateAdapter(Context context, List<WorkoutTemplate> workoutTemplates) {
        this.mContext = context;
        this.workoutTemplates = workoutTemplates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_workout, parent, false);
        return new CreateAdapter.ViewHolder(itemView, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutTemplate template = workoutTemplates.get(position);
        try {
            holder.bind(template);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return workoutTemplates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTemplateTitle;
        private TextView tvBodyParts;

        public ViewHolder(View itemView, final Context context) {
            super(itemView);
            tvTemplateTitle = itemView.findViewById(R.id.tvTemplateTitle);
            tvBodyParts = itemView.findViewById(R.id.tvBodyParts);
        }

        public void bind(WorkoutTemplate template) throws JSONException, ParseException {
            tvTemplateTitle.setText(template.getTitle());
            queryComponents(template);

            bodyParts = new ArrayList<>();
            // store body parts included in workout in a list
            for (int i = 0; i < workoutComponents.size(); i++) {
                String body = workoutComponents.get(i).getExercise().getBodyPart();
                bodyParts.add(body);
            }

            // remove any repetitions of body parts
            Set<String> set = new HashSet<>(bodyParts);
            bodyParts.clear();
            bodyParts.addAll(set);

            for (int i = 0; i < bodyParts.size(); i++) {
                tvBodyParts.append(bodyParts.get(i));
                if (i + 1 < bodyParts.size()) {
                    tvBodyParts.append(", ");   // for formatting
                }
            }
        }
    }

    private void queryComponents(WorkoutTemplate template) throws ParseException, JSONException {
        workoutComponents = new ArrayList<>();     // initialize empty list each time a query is called
        JSONArray componentArray = template.getComponents();
        ParseQuery<WorkoutComponent> componentQuery = ParseQuery.getQuery(WorkoutComponent.class);
        for (int i = 0; i < componentArray.length(); i++) {
            // grab ID to ensure only those with same id get shown
            String id = componentArray.getJSONObject(i).getString("objectId");
            // add filter to grab only components in the specific workout
            componentQuery.whereEqualTo(WorkoutComponent.KEY_OBJECT_ID, id);
            componentQuery.include(WorkoutComponent.KEY_EXERCISE);
            componentQuery.include(WorkoutComponent.KEY_REPS);
            componentQuery.include(WorkoutComponent.KEY_SETS);
            workoutComponents.addAll(componentQuery.find());  // add objects to list
        }
    }
}
