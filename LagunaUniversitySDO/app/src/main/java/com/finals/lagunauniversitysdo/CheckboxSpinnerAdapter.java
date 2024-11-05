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

import java.util.List;

public class CheckboxSpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

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
        textView.setText(values.get(position));

        // Determine if the item is a violation (header) or a type entry
        if (isViolation(position)) {
            // Bold text for violation headers
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setClickable(true); // Disable click for headers
            rowView.setClickable(true); // Ensure the entire row is non-clickable for headers
        } else {
            // Regular text for type entries
            textView.setTypeface(Typeface.DEFAULT);

            // Set click listener for type entries
            rowView.setOnClickListener(v -> {
                // Show a toast with the selected type
                String selectedType = values.get(position).trim(); // Trim to remove leading spaces
                Toast.makeText(context, "Selected Type: " + selectedType, Toast.LENGTH_SHORT).show();

                // Notify the listener about the selected type
                if (context instanceof SpinnerSelectionListener) {
                    ((SpinnerSelectionListener) context).onItemSelected(selectedType);
                }
            });
            textView.setClickable(false);
            rowView.setClickable(false); // Allow the row to be clickable for type entries
        }

        return rowView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Use the first item view as the main spinner view
        View rowView = LayoutInflater.from(context).inflate(R.layout.spinner_item_with_checkbox, parent, false);
        TextView textView = rowView.findViewById(R.id.violationText);
        textView.setText(values.get(position));

        // Set the typeface based on whether it's a header or type entry
        if (isViolation(position)) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            textView.setTypeface(Typeface.DEFAULT);
        }

        return rowView;
    }

    private boolean isViolation(int position) {
        // Assuming violation headers are not prefixed with space
        return !values.get(position).startsWith(" ");
    }

    // Interface for communication with the parent activity
    public interface SpinnerSelectionListener {
        void onItemSelected(String selectedItem);
    }
}
