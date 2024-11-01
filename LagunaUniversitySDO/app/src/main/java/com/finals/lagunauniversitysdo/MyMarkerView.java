package com.finals.lagunauniversitysdo;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class MyMarkerView extends MarkerView {
    private TextView tvContent;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent); // Assuming your layout has a TextView with this ID
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Get the value from the entry and display it
        tvContent.setText(String.valueOf((int)e.getY())); // Display the Y value (count)
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight()); // Center the marker
    }
}

