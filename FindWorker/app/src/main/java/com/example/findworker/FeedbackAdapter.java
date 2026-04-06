package com.example.findworker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedbackAdapter
        extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private List<Feedback> list;

    public FeedbackAdapter(WorkerDetailsActivity workerDetailsActivity, List<Feedback> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        Feedback f = list.get(i);

        h.userName.setText(f.userName);
        h.feedback.setText(f.feedback);
        h.rating.setText("★".repeat((int) f.rating));

        CharSequence time =
                android.text.format.DateUtils.getRelativeTimeSpanString(
                        f.time,
                        System.currentTimeMillis(),
                        android.text.format.DateUtils.MINUTE_IN_MILLIS
                );
        h.time.setText(time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, rating, feedback, time;

        ViewHolder(View v) {
            super(v);
            userName = v.findViewById(R.id.txtUserName);
            rating = v.findViewById(R.id.txtRating);
            feedback = v.findViewById(R.id.txtFeedback);
            time = v.findViewById(R.id.txtTime);
        }
    }
}
