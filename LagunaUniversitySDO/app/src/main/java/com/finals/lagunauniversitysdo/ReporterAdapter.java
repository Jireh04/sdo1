package com.finals.lagunauniversitysdo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReporterAdapter extends RecyclerView.Adapter<ReporterAdapter.ReporterViewHolder> {
    private List<String> reporterList;
    private final OnReporterClickListener onReporterClickListener;

    public ReporterAdapter(List<String> reporterList, OnReporterClickListener onReporterClickListener) {
        this.reporterList = reporterList;
        this.onReporterClickListener = onReporterClickListener;
    }

    @NonNull
    @Override
    public ReporterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reporter_item, parent, false);
        return new ReporterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReporterViewHolder holder, int position) {
        String reporterName = reporterList.get(position);
        holder.bind(reporterName, onReporterClickListener);
    }

    @Override
    public int getItemCount() {
        return reporterList.size();
    }

    public void updateData(List<String> newReporterList) {
        reporterList.clear();
        reporterList.addAll(newReporterList);
        notifyDataSetChanged();
    }

    // Listener interface for handling click events on the "View Logs" button
    public interface OnReporterClickListener {
        void onReporterClick(String reporterName);
    }

    static class ReporterViewHolder extends RecyclerView.ViewHolder {
        private final TextView reporterNameTextView;
        private final Button viewLogsButton;

        public ReporterViewHolder(@NonNull View itemView) {
            super(itemView);
            reporterNameTextView = itemView.findViewById(R.id.reporterName);
            viewLogsButton = itemView.findViewById(R.id.viewLogsButton);
        }

        // Bind the reporter name and set up the button click listener
        public void bind(String reporterName, OnReporterClickListener listener) {
            reporterNameTextView.setText(reporterName);

            // Set up the button to call the listener with the reporter's name when clicked
            viewLogsButton.setOnClickListener(v -> listener.onReporterClick(reporterName));
        }
    }
}
