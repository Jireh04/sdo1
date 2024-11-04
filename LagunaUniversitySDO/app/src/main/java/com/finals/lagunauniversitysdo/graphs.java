package com.finals.lagunauniversitysdo;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class graphs {

    private BarChart barChart;
    private LineChart lineChart;
    private FirebaseFirestore db;

    // Maps to hold combined data
    private Map<String, Integer> totalStudentCountMap = new HashMap<>();
    private Map<String, Integer> lightOffenseCountMap = new HashMap<>();
    private Map<String, Integer> seriousOffenseCountMap = new HashMap<>();
    private Map<String, Integer> majorOffenseCountMap = new HashMap<>();

    public graphs(View rootView) {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Find chart views by ID
        barChart = rootView.findViewById(R.id.barchart);
        lineChart = rootView.findViewById(R.id.linechart);
    }

    public void fetchDataFromFirestore() {
        // Fetch data from nested collections in "students" and "personnel"
        fetchNestedCollectionData("students", "student_refferal_history");
        fetchNestedCollectionData("personnel", "personnel_refferal_history");

        // Fetch data from standalone collection "prefect_refferal_history"
        fetchCollectionData("prefect_refferal_history");
    }

    private void fetchNestedCollectionData(String mainCollectionName, String subCollectionName) {
        db.collection(mainCollectionName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Access the nested subcollection for each document
                                db.collection(mainCollectionName)
                                        .document(document.getId())
                                        .collection(subCollectionName)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> subTask) {
                                                if (subTask.isSuccessful()) {
                                                    // Process the data for this subcollection
                                                    processFirestoreData(subTask.getResult());
                                                } else {
                                                    // Handle error for subcollection fetch
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Handle error for main collection fetch
                        }
                    }
                });
    }

    private void fetchCollectionData(String collectionName) {
        db.collection(collectionName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Process the data for this collection
                            processFirestoreData(task.getResult());
                        } else {
                            // Handle error
                        }
                    }
                });
    }


    public void processFirestoreData(QuerySnapshot querySnapshot) {
        // For counting unique students per program (BarChart)
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            String studentProgram = document.getString("student_program");
            String violation = document.getString("violation");
            Object dateObj = document.get("date");

            // Counting unique students per program (BarChart)
            if (studentProgram != null) {
                totalStudentCountMap.put(studentProgram, totalStudentCountMap.getOrDefault(studentProgram, 0) + 1);
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

        // Once all data is processed, update charts
        if (!querySnapshot.isEmpty()) {
            setupBarChart();
            setupLineChart();
        }

        lineChart.animateX(1000);  // Animates X-axis over 1 second
        barChart.animateY(1000);   // Animates Y-axis over 1.5 seconds

    }

    // Example color list (you can customize these colors)
    private int[] programColors = {
            Color.parseColor("#A8E6CF"), // Program 1
            Color.parseColor("#8DAEFB"), // Program 2
            Color.parseColor("#FF4C4C"), // Program 3
            Color.parseColor("#28a745"), // Program 4
            Color.CYAN,                  // Program 5
            Color.MAGENTA                // Program 6
    };

    public void setupBarChart() {
        List<BarEntry> programEntries = new ArrayList<>();
        ArrayList<String> programLabels = new ArrayList<>();
        int index = 0;

        // Create a list to hold the colors for each dataset
        ArrayList<Integer> programColorsList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : totalStudentCountMap.entrySet()) {
            String studentProgram = entry.getKey();
            int count = entry.getValue();

            programLabels.add(studentProgram); // Add program name to labels
            programEntries.add(new BarEntry(index, count)); // Add student count

            // Assign a color based on the index or cycle through the available colors
            int colorIndex = index % programColors.length; // Ensure we cycle through available colors
            programColorsList.add(programColors[colorIndex]);

            index++;
        }

        // Create and customize the BarDataSet
        BarDataSet studentDataSet = new BarDataSet(programEntries, "Number of Students with Violations");
        studentDataSet.setColors(programColorsList); // Set individual colors for each bar

        BarData barData = new BarData(studentDataSet);
        barChart.setData(barData);

        // Set the ValueFormatter for the Y-axis
        barChart.getAxisLeft().setValueFormatter(new IntegerValueFormatter());
        barChart.getAxisRight().setValueFormatter(new IntegerValueFormatter());

        // Set the chart description
        barChart.getDescription().setText("Number of Students with Violations per Program");
        barChart.getDescription().setTextSize(12f); // Adjust the size as needed
        barChart.getDescription().setTextAlign(Paint.Align.CENTER);
        barChart.getDescription().setPosition(barChart.getWidth() * 0.5f , 25);
        barChart.getDescription().setTextColor(Color.parseColor("#333333"));


        // Customize X-Axis to display program names
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(programLabels));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setLabelCount(programLabels.size(), true);
        barChart.getAxisLeft().setGranularity(1f);

        // Set text color for X-axis labels
        barChart.getXAxis().setTextColor(Color.parseColor("#333333"));
        barChart.getXAxis().setGridColor(Color.parseColor("#E5E5E5"));
        barChart.getAxisLeft().setGridColor(Color.parseColor("#E5E5E5"));


        // Refresh the chart with new data
        barChart.invalidate();
    }

    public void setupLineChart() {
        List<Entry> lightOffenseEntries = new ArrayList<>();
        List<Entry> seriousOffenseEntries = new ArrayList<>();
        List<Entry> majorOffenseEntries = new ArrayList<>();
        List<String> months = new ArrayList<>();
        int index = 0;

        // Sort the months to display them in chronological order
        List<String> sortedMonths = new ArrayList<>(lightOffenseCountMap.keySet());
        Collections.sort(sortedMonths);

        // Collect monthly data for each offense type
        for (String monthYear : sortedMonths) {
            lightOffenseEntries.add(new Entry(index, lightOffenseCountMap.getOrDefault(monthYear, 0)));
            seriousOffenseEntries.add(new Entry(index, seriousOffenseCountMap.getOrDefault(monthYear, 0)));
            majorOffenseEntries.add(new Entry(index, majorOffenseCountMap.getOrDefault(monthYear, 0)));
            months.add(monthYear); // Add month-year to labels
            index++;
        }

        // Create and customize the LineChart datasets with colors and point styles
        LineDataSet lightOffenseDataSet = new LineDataSet(lightOffenseEntries, "Light Offense");
        lightOffenseDataSet.setColor(Color.parseColor("#A8E6CF")); // Light green color
        lightOffenseDataSet.setDrawCircleHole(false);
        lightOffenseDataSet.setCircleColor(Color.parseColor("#A8E6CF"));
        lightOffenseDataSet.setDrawValues(false);
        lightOffenseDataSet.setDrawCircles(true);
        lightOffenseDataSet.setCircleRadius(5f);
        lightOffenseDataSet.setCircleHoleRadius(2.5f);
        lightOffenseDataSet.setLineWidth(2.5f);


        LineDataSet seriousOffenseDataSet = new LineDataSet(seriousOffenseEntries, "Serious Offense");
        seriousOffenseDataSet.setColor(Color.parseColor("#8DAEFB")); // Blue color
        seriousOffenseDataSet.setDrawCircleHole(false);
        seriousOffenseDataSet.setCircleColor(Color.parseColor("#8DAEFB"));
        seriousOffenseDataSet.setDrawValues(false);
        seriousOffenseDataSet.setDrawCircles(true);
        seriousOffenseDataSet.setCircleRadius(5f);
        seriousOffenseDataSet.setCircleHoleRadius(2.5f);
        seriousOffenseDataSet.setLineWidth(2.5f);


        LineDataSet majorOffenseDataSet = new LineDataSet(majorOffenseEntries, "Major Offense");
        majorOffenseDataSet.setColor(Color.parseColor("#FF4C4C")); // Red color
        majorOffenseDataSet.setDrawCircleHole(false);
        majorOffenseDataSet.setCircleColor(Color.parseColor("#FF4C4C"));
        majorOffenseDataSet.setDrawValues(false);
        majorOffenseDataSet.setDrawCircles(true);
        majorOffenseDataSet.setCircleRadius(5f);
        majorOffenseDataSet.setCircleHoleRadius(2.5f);
        majorOffenseDataSet.setLineWidth(2.5f);


        // Combine datasets into LineData
        LineData lineData = new LineData(lightOffenseDataSet, seriousOffenseDataSet, majorOffenseDataSet);
        lineChart.setData(lineData);

    // Set Y-axis value formatter
        lineChart.getAxisLeft().setValueFormatter(new IntegerValueFormatter());
        lineChart.getAxisRight().setEnabled(false); // Disable the right Y-axis if not needed

    // Customize the X-axis
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setLabelCount(months.size(), true);
        lineChart.getXAxis().setTextColor(Color.parseColor("#333333"));
        lineChart.getXAxis().setGridColor(Color.parseColor("#E5E5E5"));
        lineChart.getAxisLeft().setGridColor(Color.parseColor("#E5E5E5"));

    // Set chart description
        lineChart.getDescription().setText("Monthly Violations by Type");
        lineChart.getDescription().setTextAlign(Paint.Align.CENTER);
        lineChart.getDescription().setTextSize(12f);
        lineChart.getDescription().setPosition(lineChart.getWidth() * 0.5f, 25);
        lineChart.getDescription().setTextColor(Color.parseColor("#333333"));

    // Configure the legend
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.parseColor("#333333"));
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setYEntrySpace(10f); // Adjust vertical spacing
        legend.setYOffset(10f); // Add top margin for the legend

    // Set up MarkerView to display data counts
        MyMarkerView markerView = new MyMarkerView(lineChart.getContext(), R.layout.custom_marker_view);
        lineChart.setMarker(markerView); // Set the marker to the chart

    // Refresh the chart with new data
        lineChart.invalidate();

    }



    public class IntegerValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
 }


}


