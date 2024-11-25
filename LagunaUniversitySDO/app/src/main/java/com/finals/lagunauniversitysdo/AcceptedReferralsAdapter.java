package com.finals.lagunauniversitysdo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class AcceptedReferralsAdapter extends RecyclerView.Adapter<AcceptedReferralsAdapter.ViewHolder> {
    private List<DocumentSnapshot> referrals;

    public AcceptedReferralsAdapter(List<DocumentSnapshot> referrals) {
        this.referrals = referrals;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, referralDate, status;

        public ViewHolder(View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.text_student_name);
            referralDate = itemView.findViewById(R.id.text_referral_date);
            status = itemView.findViewById(R.id.text_referral_status);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.accepted_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DocumentSnapshot referral = referrals.get(position);
        holder.studentName.setText("NAME: " + referral.getString("student_name"));
        holder.referralDate.setText("DATE: " + referral.getString("date"));
        holder.status.setText("STATUS: " + "ACCEPTED");
    }

    @Override
    public int getItemCount() {
        return referrals.size();
    }
}

