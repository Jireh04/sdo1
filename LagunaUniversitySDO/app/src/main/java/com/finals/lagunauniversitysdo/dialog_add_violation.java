package com.finals.lagunauniversitysdo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class dialog_add_violation extends DialogFragment {

    private EditText dateTimeEditText;
    private Button pickDateTimeButton;
    private OnViolationAddedListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_violation, container, false);

        dateTimeEditText = view.findViewById(R.id.dateTime_edit_text);
        pickDateTimeButton = view.findViewById(R.id.pick_date_time_button);

        pickDateTimeButton.setOnClickListener(v -> showDateTimePicker());

        return view;
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            final Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, monthOfYear, dayOfMonth);
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDate.set(Calendar.MINUTE, minute);
                String dateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(selectedDate.getTime());
                dateTimeEditText.setText(dateTimeString);
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public interface OnViolationAddedListener {
        void onViolationAdded(Violation violation);
    }

    public void setOnViolationAddedListener(OnViolationAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_violation, null);

        final EditText studentNoEditText = view.findViewById(R.id.student_no_edit_text);
        final EditText studentNameEditText = view.findViewById(R.id.student_name_edit_text);
        final EditText blockEditText = view.findViewById(R.id.block_edit_text);
        final EditText offenseTypeEditText = view.findViewById(R.id.offense_type_edit_text);
        final EditText remarksEditText = view.findViewById(R.id.remarks_edit_text);
        final EditText violationEditText = view.findViewById(R.id.violation_edit_text);
        final EditText locationEditText = view.findViewById(R.id.location_edit_text);
        final EditText referrerEditText = view.findViewById(R.id.referrer_edit_text);
        dateTimeEditText = view.findViewById(R.id.dateTime_edit_text);
        pickDateTimeButton = view.findViewById(R.id.pick_date_time_button);

        pickDateTimeButton.setOnClickListener(v -> showDateTimePicker());

        // Retrieve and set the data from the QR code
        Bundle bundle = getArguments();
        if (bundle != null) {
            studentNoEditText.setText(bundle.getString("studentNo"));
            studentNameEditText.setText(bundle.getString("studentName"));
            blockEditText.setText(bundle.getString("block"));

            // Make fields non-editable
            studentNoEditText.setEnabled(false);
            studentNameEditText.setEnabled(false);
            blockEditText.setEnabled(false);
        }

        builder.setView(view)
                .setTitle("Add Violation")
                .setNegativeButton("Cancel", (dialog, id) -> dialog_add_violation.this.getDialog().cancel())
                .setPositiveButton("Add", null);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String studentNo = studentNoEditText.getText().toString();
                String studentName = studentNameEditText.getText().toString();
                String block = blockEditText.getText().toString();
                String offenseType = offenseTypeEditText.getText().toString();
                String remarks = remarksEditText.getText().toString();
                String violations = violationEditText.getText().toString();
                String location = locationEditText.getText().toString();
                String referrer = referrerEditText.getText().toString();
                String dateTime = dateTimeEditText.getText().toString();

                if (TextUtils.isEmpty(studentNo) || TextUtils.isEmpty(studentName) || TextUtils.isEmpty(block) || TextUtils.isEmpty(offenseType) || TextUtils.isEmpty(remarks) || TextUtils.isEmpty(violations) || TextUtils.isEmpty(location) || TextUtils.isEmpty(referrer) || TextUtils.isEmpty(dateTime)) {
                    Toast.makeText(getContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    Timestamp timestamp = convertToTimestamp(dateTime);
                    Violation violation = new Violation(studentNo, studentName, block, offenseType, remarks, location, violations, referrer, timestamp);
                    if (listener != null) {
                        listener.onViolationAdded(violation);
                    }
                    dialog.dismiss();
                }
            });
        });

        return dialog;
    }

    private Timestamp convertToTimestamp(String dateTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            java.util.Date date = format.parse(dateTime);
            return new Timestamp(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
