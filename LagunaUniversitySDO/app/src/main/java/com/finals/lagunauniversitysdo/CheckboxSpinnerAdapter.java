package com.finals.lagunauniversitysdo;

import android.content.Context;
import android.graphics.Typeface;
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
    private Integer selectedViolationIndex = null;

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
            // Display the violation as a header without a checkbox
            checkBox.setVisibility(View.GONE);
            textView.setTypeface(Typeface.DEFAULT_BOLD); // Bold text for violation headers
            textView.setOnClickListener(view -> {
                // Set this as the selected violation index
                selectedViolationIndex = position;
                // Reset all checkboxes except those related to the selected violation
                resetCheckboxStatesForSelectedViolation();
                notifyDataSetChanged(); // Refresh the dropdown list to show/hide items
            });
        } else {
            // Show only types related to the selected violation
            boolean isVisible = isTypeOfSelectedViolation(position);
            rowView.setVisibility(isVisible ? View.VISIBLE : View.GONE); // Hide types not related to selected violation
            checkBox.setEnabled(isVisible);

            // Avoid multiple listeners by clearing any existing listener first
            checkBox.setOnCheckedChangeListener(null);
            if (isVisible) {
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    checkboxStates.put(position, isChecked); // Update checkbox state
                });
            }
        }

        return rowView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent); // Use the same layout for the main spinner view
    }

    private boolean isViolation(int position) {
        return !values.get(position).startsWith(" "); // Assuming types are prefixed with " "
    }

    private boolean isTypeOfSelectedViolation(int position) {
        // Check if the position belongs to the selected violation
        return selectedViolationIndex != null && position > selectedViolationIndex && (isViolation(position) || position < values.size());
    }

    private void resetCheckboxStatesForSelectedViolation() {
        // Uncheck all types, but keep the selected violation types checked if previously checked
        for (int i = 0; i < values.size(); i++) {
            if (!isViolation(i) && isTypeOfSelectedViolation(i)) {
                checkboxStates.put(i, false); // Uncheck all types of other violations
            }
        }
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
