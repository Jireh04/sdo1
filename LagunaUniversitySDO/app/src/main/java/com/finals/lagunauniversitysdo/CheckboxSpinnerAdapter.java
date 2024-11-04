package com.finals.lagunauniversitysdo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class CheckboxSpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;
    private final Map<Integer, Boolean> checkboxStates;
    private Integer selectedViolationIndex = null; // To track the selected violation

    public CheckboxSpinnerAdapter(Context context, List<String> values) {
        super(context, R.layout.spinner_item_with_checkbox, values);
        this.context = context;
        this.values = values;
        this.checkboxStates = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            checkboxStates.put(i, false); // Initialize all checkboxes to unchecked
        }
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.spinner_item_with_checkbox, parent, false);

        TextView textView = rowView.findViewById(R.id.violationText);
        CheckBox checkBox = rowView.findViewById(R.id.violationCheckbox);

        textView.setText(values.get(position));
        checkBox.setChecked(checkboxStates.get(position)); // Restore the checkbox state

        if (isViolation(position)) {
            // Hide checkbox and make violation text not clickable
            checkBox.setVisibility(View.GONE); // Hide checkbox for violation names
            textView.setOnClickListener(null); // Prevent any click actions
        } else {
            // Enable types only if a violation is selected and they belong to it
            boolean isEnabled = selectedViolationIndex != null && isTypeOfSelectedViolation(position);
            checkBox.setEnabled(isEnabled);
            checkBox.setChecked(checkboxStates.get(position)); // Restore checkbox state

            // Avoid multiple listeners being set by clearing any existing listener first
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (selectedViolationIndex != null && isTypeOfSelectedViolation(position)) {
                    checkboxStates.put(position, isChecked); // Update checkbox state
                }
            });
        }

        return rowView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent); // Use the same layout for the main spinner view
    }

    private boolean isViolation(int position) {
        // Check if the current position corresponds to a violation name
        return !values.get(position).startsWith(" "); // Assuming types are prefixed with "  - "
    }

    private boolean isTypeOfSelectedViolation(int position) {
        // Check if the current position corresponds to a type of the currently selected violation
        if (selectedViolationIndex == null) return false;
        // Check if the position is greater than the selected violation index
        return position > selectedViolationIndex && position < values.size(); // Ensure it's still within bounds
    }

    public List<String> getSelectedItems() {
        List<String> selectedItems = new ArrayList<>();
        for (Map.Entry<Integer, Boolean> entry : checkboxStates.entrySet()) {
            if (entry.getValue()) {
                selectedItems.add(values.get(entry.getKey()));
            }
        }
        return selectedItems;
    }
}
