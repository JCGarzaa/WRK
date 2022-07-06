package com.example.wrk;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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

    public void removeItem(int position) {
        mComponents.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(WorkoutComponent component, int position) {
        mComponents.add(position, component);
        notifyItemInserted(position);
    }

    public ArrayList<WorkoutComponent> getData() {
        return (ArrayList<WorkoutComponent>) mComponents;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TableLayout tlComponents;
        private TextView tvExerciseName;
        private Button btnAddSet;
        public TextView tvPrevWeight;
        public EditText etWeight;
        public EditText etReps;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tlComponents = itemView.findViewById(R.id.tlComponents);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
        }

        public void bind(WorkoutComponent component) {
            Context tableContext = tlComponents.getContext();
            tvExerciseName.setText(component.getExercise().getName());
            // clear any existing rows starting after exercise name and column headers
            tlComponents.removeViews(2, Math.max(0, tlComponents.getChildCount()) - 2); // 2 is starting row index with 0 and 1 being exercise name and column headers
            // create new row for each existing set
            for (int i = 1; i <= component.getSets(); i++) {
                // create new row for each set
                TableRow tbrow = new TableRow(tableContext);
                // For each row, add 2 TextViews for set number and previous weight, 2 editText for weight, and reps
                // set number column
                TextView tvSetNum = new TextView(tableContext);
                tvSetNum.setText(String.valueOf(i));
                tbrow.addView(tvSetNum);
                // previous weight column
                tvPrevWeight = new TextView(tableContext);
                tvPrevWeight.setText(String.valueOf(component.getWeight()));
                tbrow.addView(tvPrevWeight);
                // weight column
                etWeight = new EditText(tableContext);
                etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                etWeight.setHint(tvPrevWeight.getText());       // set a placeholder with previous weight
                // update weight for database based on what user inputs
                etWeight.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            component.setWeight(Double.parseDouble(s.toString()));
                        }
                        catch (NumberFormatException n) {}

                    }
                });
                tbrow.addView(etWeight);
                // reps column
                etReps = new EditText(tableContext);
                etReps.setInputType(InputType.TYPE_CLASS_NUMBER);
                etReps.setHint(String.valueOf(component.getReps()));            // set a placeholder with previous reps
                // update reps for database based on what user inputs
                etReps.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            component.setReps(Integer.valueOf(s.toString()));
                        }
                        catch (NumberFormatException n) {}
                    }
                });
                tbrow.addView(etReps);
                tlComponents.addView(tbrow);
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
                    TextView tvPrevAmount = new TextView(tableContext);   // has to be different from tvPrevWeight so it doesn't display weight from current sets when adding sets
                    tvPrevAmount.setText(tvPrevWeight.getText());
                    tbrow.addView(tvPrevAmount);
                    // weight column
                    etWeight = new EditText(tableContext);
                    etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    etWeight.setText(String.valueOf(component.getWeight()));        // set a placeholder with weight from existing sets
                    tbrow.addView(etWeight);
                    // reps column
                    etReps = new EditText(tableContext);
                    etReps.setInputType(InputType.TYPE_CLASS_NUMBER);
                    etReps.setText(String.valueOf(component.getReps()));            // set a placeholder with reps from previous sets
                    tbrow.addView(etReps);
                    tlComponents.addView(tbrow);
                }
            });
        }
    }
}
