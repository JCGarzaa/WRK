package com.example.wrk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wrk.models.WorkoutComponent;
import com.parse.ParseException;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

// Adapter is used for the recyclerview in the ScratchCreateActivity to add exercises
public class ScratchCreateAdapter extends RecyclerView.Adapter<ScratchCreateAdapter.ViewHolder> {
    Context mContext;
    List<WorkoutComponent> mComponents;

    public ScratchCreateAdapter(Context mContext, ArrayList<WorkoutComponent> mComponents) {
        this.mContext = mContext;
        this.mComponents = mComponents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(mContext).inflate(R.layout.item_scratch_component, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutComponent component = mComponents.get(position);
        holder.bind(component);
    }

    @Override
    public int getItemCount() {
        return mComponents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TableLayout tlComponents;
        private TextView tvExerciseName;
        private Button btnAddSet;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tlComponents = itemView.findViewById(R.id.tlComponents);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
        }

        public void bind(WorkoutComponent component) {
            Context tableContext = tlComponents.getContext();
            tvExerciseName.setText(component.getExercise().getName());
            for (int i = 0; i < component.getSets(); i++) {
                // create new row for each set
                TableRow tbrow = new TableRow(tableContext);
                // For each row, add 2 TextViews for set number and previous weight, 2 editText for weight, and reps
                // set number column
                TextView tvSetNum = new TextView(tableContext);
                tvSetNum.setText(String.valueOf(i));
                tbrow.addView(tvSetNum);
                // previous weight column
                TextView tvPrevWeight = new TextView(tableContext);
                tvPrevWeight.setText(String.valueOf(component.getWeight()));
                tbrow.addView(tvPrevWeight);
                // weight column
                EditText etWeight = new EditText(tableContext);
                etWeight.setHint(tvPrevWeight.getText());       // set a placeholder with previous weight
                tbrow.addView(etWeight);
                // reps column
                EditText etReps = new EditText(tableContext);
                etReps.setText(component.getReps());            // set a placeholder with previous reps
                tbrow.addView(etReps);
            }
            btnAddSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // create new row for the set
                    TableRow tbrow = new TableRow(tableContext);
                    // For each row, add 2 TextViews for set number and previous weight, 2 editText for weight, and reps
                    // set number column
                    TextView tvSetNum = new TextView(tableContext);
                    tvSetNum.setText(String.valueOf(component.getSets() + 1));
                    component.setSets(component.getSets() + 1);     // update number of sets for component
                    tbrow.addView(tvSetNum);
                    // previous weight column
                    TextView tvPrevWeight = new TextView(tableContext);
                    tvPrevWeight.setText(String.valueOf(component.getWeight()));
                    tbrow.addView(tvPrevWeight);
                    // weight column
                    EditText etWeight = new EditText(tableContext);
                    etWeight.setHint(tvPrevWeight.getText());       // set a placeholder with previous weight
                    tbrow.addView(etWeight);
                    // reps column
                    EditText etReps = new EditText(tableContext);
                    etReps.setText(component.getReps());            // set a placeholder with previous reps
                    tbrow.addView(etReps);
                }
            });
        }
    }
}
