package com.finals.lagunauniversitysdo;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class CheckboxSpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    // Track the currently selected violation and type
    private String selectedViolation = null;
    private String selectedType = null;

    public CheckboxSpinnerAdapter(Context context, List<String> values) {
        super(context, R.layout.spinner_item_with_checkbox, values);
        this.context = context;
        this.values = values;
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.spinner_item_with_checkbox, parent, false);

        TextView textView = rowView.findViewById(R.id.violationText);
        String itemText = values.get(position).trim(); // Trim to handle indentation
        textView.setText(itemText);

        // Prevent selecting "Select a Violation"
        if (itemText.equals("Select a Violation")) {
            textView.setTextColor(context.getResources().getColor(android.R.color.darker_gray)); // Gray out
            rowView.setOnClickListener(null); // Disable selection for "Select a Violation"
        } else if (isViolation(position)) {
            // If it's a violation header
            textView.setTypeface(Typeface.DEFAULT_BOLD);

            // Set click listener for the violation header
            rowView.setOnClickListener(v -> {
                if (selectedViolation == null) {
                    // Select this violation if none selected yet
                    selectedViolation = itemText;
                    Toast.makeText(context, "Selected Violation: " + itemText, Toast.LENGTH_SHORT).show();
                } else if (selectedViolation.equals(itemText)) {
                    // Deselect if it's already selected
                    selectedViolation = null;
                    selectedType = null;  // Reset type selection as well
                    Toast.makeText(context, "Deselected Violation: " + itemText, Toast.LENGTH_SHORT).show();
                } else {
                    // Notify user they can only select one violation
                    Toast.makeText(context, "You can only select one violation at a time", Toast.LENGTH_SHORT).show();
                }
                notifyDataSetChanged(); // Refresh the view to reflect selection changes
            });
        } else {
            // If it's a type entry
            textView.setTypeface(Typeface.DEFAULT);

            // Set click listener for types if they belong to the selected violation
            if (selectedViolation != null && getAssociatedViolation(position).equals(selectedViolation)) {
                rowView.setOnClickListener(v -> {
                    selectedType = itemText;
                    Toast.makeText(context, "Selected Type: " + itemText, Toast.LENGTH_SHORT).show();

                    // Notify listener of the selected type
                    if (context instanceof SpinnerSelectionListener) {
                        ((SpinnerSelectionListener) context).onItemSelected(selectedViolation, selectedType);
                    }
                    notifyDataSetChanged(); // Refresh to update selection display
                });
            } else {
                // Disable click for types outside the selected violation
                rowView.setOnClickListener(null);
                textView.setTextColor(context.getResources().getColor(android.R.color.darker_gray)); // Gray out
            }

            // Bold the selected type within the chosen violation for better indication
            if (itemText.equals(selectedType)) {
                textView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

        return rowView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Main spinner view (selected item display)
        View rowView = LayoutInflater.from(context).inflate(R.layout.spinner_item_with_checkbox, parent, false);
        TextView textView = rowView.findViewById(R.id.violationText);

        // Prevent showing "Select a Violation" when no violation is selected
        if (selectedViolation != null && selectedType != null) {
            textView.setText(selectedViolation + " - " + selectedType);
        } else if (selectedViolation != null) {
            textView.setText(selectedViolation); // Only show violation if type isn't selected
        } else {
            textView.setText("Select a Violation"); // Default text if no selection
        }

        return rowView;
    }

    private boolean isViolation(int position) {
        // Checks if the item is a header (assuming headers are not prefixed with space)
        return !values.get(position).startsWith(" ");
    }

    private String getAssociatedViolation(int position) {
        // Find the nearest violation header above this position
        for (int i = position - 1; i >= 0; i--) {
            if (isViolation(i)) {
                return values.get(i).trim();
            }
        }
        return "";
    }

    // Getter methods for selected violation and type
    public String getSelectedViolation() {
        return selectedViolation;
    }

    public String getSelectedType() {
        return selectedType;
    }

    // Interface for communication with the parent activity
    public interface SpinnerSelectionListener {
        void onItemSelected(String violation, String selectedType);
    }
}
