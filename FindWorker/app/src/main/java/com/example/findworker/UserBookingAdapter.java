package com.example.findworker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserBookingAdapter
        extends RecyclerView.Adapter<UserBookingAdapter.ViewHolder> {

    private Context context;
    private List<UserBooking> bookingList;

    public UserBookingAdapter(Context context, List<UserBooking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        UserBooking booking = bookingList.get(position);

        holder.workerName.setText(booking.getWorkerName());

        String status = booking.getStatus() != null
                ? booking.getStatus().toUpperCase()
                : "PENDING";

        holder.status.setText(status);
        applyStatusStyle(holder.status, status);

        SimpleDateFormat sdf =
                new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

        holder.date.setText(
                "Booked on " + sdf.format(new Date(booking.getTimestamp()))
        );

        // ✅ CLICK → OPEN DETAILS PAGE
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, BookingDetailsActivity.class);
            i.putExtra("requestId", booking.getRequestId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    // 🎨 STATUS STYLE (Flipkart / Amazon feel)
    private void applyStatusStyle(TextView statusView, String status) {

        switch (status) {

            case "PENDING":
                statusView.setTextColor(Color.parseColor("#FF9800"));
                statusView.setBackgroundResource(R.drawable.bg_status_pending);
                break;

            case "ACCEPTED":
            case "STARTED":
                statusView.setTextColor(Color.parseColor("#4CAF50"));
                statusView.setBackgroundResource(R.drawable.bg_status_active);
                break;


            case "REACHED":
                statusView.setTextColor(Color.parseColor("#388E3C"));
                statusView.setBackgroundResource(R.drawable.bg_status_reached);
                break;

            case "COMPLETED":
                statusView.setTextColor(Color.parseColor("#0288D1"));
                statusView.setBackgroundResource(R.drawable.bg_status_completed);
                break;

            case "REJECTED":
                statusView.setTextColor(Color.parseColor("#D32F2F"));
                statusView.setBackgroundResource(R.drawable.bg_status_rejected);
                break;
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView workerName, status, date;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            workerName = itemView.findViewById(R.id.bookingWorkerName);
            status = itemView.findViewById(R.id.bookingStatus);
            date = itemView.findViewById(R.id.bookingDate);
        }
    }
}
