package com.finals.lagunauniversitysdo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class RejectedReferralsAdapter extends RecyclerView.Adapter<RejectedReferralsAdapter.ViewHolder> {
    private final List<DocumentSnapshot> rejectedReferrals;

    public RejectedReferralsAdapter(List<DocumentSnapshot> rejectedReferrals) {
        this.rejectedReferrals = rejectedReferrals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.referral_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot document = rejectedReferrals.get(position);

        String studentName = document.getString("student_name");
        String referralDate = document.getString("date");

        holder.studentNameTextView.setText("NAME: " + studentName);
        holder.referralDateTextView.setText("DATE: " + referralDate);
        holder.statusTextView.setText("STATUS: "+ "REJECTED");
        holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
    }


    @Override
    public int getItemCount() {
        return rejectedReferrals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentNameTextView;
        TextView referralDateTextView;
        TextView statusTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentNameTextView = itemView.findViewById(R.id.text_view_student_name);
            referralDateTextView = itemView.findViewById(R.id.text_view_referral_date);
            statusTextView = itemView.findViewById(R.id.text_view_status);
        }
    }
}

