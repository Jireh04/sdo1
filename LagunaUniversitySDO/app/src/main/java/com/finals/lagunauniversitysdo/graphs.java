package com.finals.lagunauniversitysdo;

import android.graphics.Color;

import androidx.annotation.NonNull;

import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class graphs {

    private BarChart barChart;
    private LineChart lineChart;
    private FirebaseFirestore db;

    public graphs(View rootView) {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Find chart views by ID
        barChart = rootView.findViewById(R.id.barchart);
        lineChart = rootView.findViewById(R.id.linechart);
    }

    public void fetchDataFromFirestore() {
        db.collection("student_refferal_history")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Process data
                            processFirestoreData(task.getResult());
                        } else {
                            // Handle error
                        }
                    }
                });
    }

    public void processFirestoreData(QuerySnapshot querySnapshot) {
        // For counting students with violations per program (BarChart)
        Map<String, Integer> studentCountMap = new HashMap<>();

        // For counting violations per month (LineChart)
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        Map<String, Integer> lightOffenseCountMap = new HashMap<>();
        Map<String, Integer> seriousOffenseCountMap = new HashMap<>();
        Map<String, Integer> majorOffenseCountMap = new HashMap<>();

        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            String studentProgram = document.getString("student_program");
            String violation = document.getString("violation");
            String studentId = document.getId(); // Use document ID as unique identifier for student
            Object dateObj = document.get("date");

            // Counting unique students per program (BarChart)
            if (studentProgram != null) {
                studentCountMap.put(studentProgram, studentCountMap.getOrDefault(studentProgram, 0) + 1);
            }

            // Counting violations per month (LineChart)
            if (dateObj != null) {
                Date date = null;
                if (dateObj instanceof String) {
                    try {
                        date = inputFormat.parse((String) dateObj);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (dateObj instanceof Timestamp) {
                    date = ((Timestamp) dateObj).toDate();
                }

                if (date != null) {
                    String monthYear = monthYearFormat.format(date);
                    switch (violation) {
                        case "Light Offense":
                            lightOffenseCountMap.put(monthYear, lightOffenseCountMap.getOrDefault(monthYear, 0) + 1);
                            break;
                        case "Serious Offense":
                            seriousOffenseCountMap.put(monthYear, seriousOffenseCountMap.getOrDefault(monthYear, 0) + 1);
                            break;
                        case "Major Offense":
                            majorOffenseCountMap.put(monthYear, majorOffenseCountMap.getOrDefault(monthYear, 0) + 1);
                            break;
                    }
                }
            }
        }

        // Prepare entries for the BarChart (per program)
        List<BarEntry> programEntries = new ArrayList<>();
        ArrayList<String> programLabels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Integer> entry : studentCountMap.entrySet()) {
            String studentProgram = entry.getKey();
            int count = entry.getValue();

            programLabels.add(studentProgram); // Add program name to labels
            programEntries.add(new BarEntry(index, count)); // Add student count
            index++;
        }

        // Set up the BarChart with the program data
        setupBarChart(programLabels, programEntries);

        // Prepare data entries for each offense type (LineChart)
        List<Entry> lightOffenseEntries = new ArrayList<>();
        List<Entry> seriousOffenseEntries = new ArrayList<>();
        List<Entry> majorOffenseEntries = new ArrayList<>();
        List<String> months = new ArrayList<>();
        index = 0;

        // Collect monthly data for each offense type
        for (String monthYear : lightOffenseCountMap.keySet()) {
            lightOffenseEntries.add(new Entry(index, lightOffenseCountMap.get(monthYear)));
            seriousOffenseEntries.add(new Entry(index, seriousOffenseCountMap.getOrDefault(monthYear, 0)));
            majorOffenseEntries.add(new Entry(index, majorOffenseCountMap.getOrDefault(monthYear, 0)));
            months.add(monthYear);
            index++;
        }

        // Set up the LineChart with the monthly data
        setupLineChart(months, lightOffenseEntries, seriousOffenseEntries, majorOffenseEntries);
    }

    // Example color list (you can customize these colors)
    private int[] programColors = {
            Color.parseColor("#A8E6CF"), // Program 1
            Color.parseColor("#8DAEFB"), // Program 2
            Color.parseColor("#FF4C4C"), // Program 3
            Color.parseColor("#28a745"), // Program 4
            Color.CYAN,                  // Program 5
            Color.MAGENTA                // Program 6
            // Add more colors if you have more programs
    };



    public void setupBarChart(ArrayList<String> programLabels, List<BarEntry> studentEntries) {
        // Create a list for colors
        List<Integer> colors = new ArrayList<>();

        // Loop through the program labels to assign colors
        for (int i = 0; i < programLabels.size(); i++) {
            // Use modulo to cycle through colors if there are more programs than colors
            colors.add(programColors[i % programColors.length]);
        }

        BarDataSet studentDataSet = new BarDataSet(studentEntries, "Number of Students with Violations");
        studentDataSet.setColors(colors); // Set the colors for the bars

        BarData barData = new BarData(studentDataSet);
        barChart.setData(barData);

        // Customize X-Axis to display program names
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(programLabels));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setLabelCount(programLabels.size(), true);

        // Customize Y-Axis
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Display as integer
            }
        });

        barChart.getAxisRight().setEnabled(false); // Disable the right Y-Axis

        // Set chart description and other aesthetics
        barChart.getDescription().setText("Number of Students with Violations per Program");
        barChart.getDescription().setTextSize(10f);

        // Set the description position to the top center
        barChart.getDescription().setPosition(
                barChart.getWidth() / 2,  // Center horizontally
                30                          // Adjust this value based on your layout (try values like 20, 30, etc.)
        );

        // Refresh the chart with new data
        barChart.invalidate();


    }



    public void setupLineChart(List<String> months, List<Entry> lightOffenseEntries, List<Entry> seriousOffenseEntries, List<Entry> majorOffenseEntries) {
        // Light Offense
        LineDataSet lightOffenseDataSet = new LineDataSet(lightOffenseEntries, "Light Offense");
        lightOffenseDataSet.setColor(Color.parseColor("#A8E6CF")); // Circle color
        lightOffenseDataSet.setCircleColor(Color.parseColor("#A8E6CF")); // Border color
        lightOffenseDataSet.setCircleRadius(5f); // Size of the circles
        lightOffenseDataSet.setValueTextColor(Color.parseColor("#444444")); // Text color

        // Serious Offense
        LineDataSet seriousOffenseDataSet = new LineDataSet(seriousOffenseEntries, "Serious Offense");
        seriousOffenseDataSet.setColor(Color.parseColor("#8DAEFB")); // Circle color
        seriousOffenseDataSet.setCircleColor(Color.parseColor("#8DAEFB")); // Border color
        seriousOffenseDataSet.setCircleRadius(5f); // Size of the circles
        seriousOffenseDataSet.setValueTextColor(Color.parseColor("#444444")); // Text color

        // Major Offense
        LineDataSet majorOffenseDataSet = new LineDataSet(majorOffenseEntries, "Major Offense");
        majorOffenseDataSet.setColor(Color.parseColor("#FF8C94")); // Circle color
        majorOffenseDataSet.setCircleColor(Color.parseColor("#FF8C94")); // Border color
        majorOffenseDataSet.setCircleRadius(5f); // Size of the circles
        majorOffenseDataSet.setValueTextColor(Color.parseColor("#444444")); // Text color

        // Create LineData and set to chart
        LineData lineData = new LineData(lightOffenseDataSet, seriousOffenseDataSet, majorOffenseDataSet);
        lineChart.setData(lineData);

        // Set chart description and other aesthetics
        lineChart.getDescription().setText("Type of Offense");
        lineChart.getDescription().setTextSize(10f);

        // Setup X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setGranularity(1f);

        // Setup Y-axis
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);

        // Use post() to ensure the view has been laid out before setting the position
        lineChart.post(() -> {
            lineChart.getDescription().setPosition(
                    lineChart.getWidth() / 2,  // Center horizontally
                    30                           // Adjust this value based on your layout (try values like 20, 30, etc.)
            );
            lineChart.invalidate(); // Refresh the chart with new data
        });

        // Refresh the chart
        lineChart.invalidate();
    }


}
