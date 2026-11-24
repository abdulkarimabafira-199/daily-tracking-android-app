package com.example.alicalendar;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    /** Callback fired when the user ticks or unticks a day's counter box. */
    public interface OnCalendarEventListener {
        void onTickChanged(CalendarDay day, boolean isTicked);
        void onDayClicked(CalendarDay day);
    }
    private final List<CalendarDay> days;
    private final OnCalendarEventListener listener;
    public CalendarAdapter(List<CalendarDay> days, OnCalendarEventListener listener) {
        this.days = days;
        this.listener = listener;
    }

    public List<CalendarDay> getDays() {
        return days;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        private final View root;
        private final TextView tvDayNumber;
        private final CheckBox cbTick;
        private final View ivNoteIndicator;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.dayCellRoot);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            cbTick = itemView.findViewById(R.id.cbTick);
            ivNoteIndicator = itemView.findViewById(R.id.ivNoteIndicator);
        }

        void bind(CalendarDay day, OnCalendarEventListener listener) {
            // Always clear the listener before mutating checked state so we
            // never fire a spurious callback while re-binding a recycled view.
            cbTick.setOnCheckedChangeListener(null);

            if (!day.isCurrentMonth()) {
                tvDayNumber.setText("");
                tvDayNumber.setTextColor(root.getResources().getColor(R.color.other_month_text, null));
                cbTick.setVisibility(View.INVISIBLE);
                ivNoteIndicator.setVisibility(View.GONE);
                root.setBackground(null);
                root.setOnClickListener(null);
                root.setClickable(false);
                return;
            }

            root.setClickable(true);
            tvDayNumber.setText(String.valueOf(day.getDayOfMonth()));
            cbTick.setVisibility(View.VISIBLE);
            cbTick.setChecked(day.isTicked());

            // Load the selector to handle state changes automatically
            ColorStateList tint = root.getContext().getResources().getColorStateList(R.color.tick_tint, null);
            cbTick.setButtonTintList(tint);

            boolean hasNote = day.getNote() != null && !day.getNote().isEmpty();
            ivNoteIndicator.setVisibility(hasNote ? View.VISIBLE : View.GONE);

            if (day.isToday()) {
                root.setBackgroundResource(R.drawable.day_cell_today_bg);
                tvDayNumber.setTextColor(root.getResources().getColor(R.color.ali_indigo, null));
            } else {
                root.setBackgroundResource(R.drawable.day_cell_bg);
                tvDayNumber.setTextColor(root.getResources().getColor(R.color.text_dark, null));
            }

            cbTick.setOnCheckedChangeListener((buttonView, isChecked) -> {
                day.setTicked(isChecked);
                if (listener != null) {
                    listener.onTickChanged(day, isChecked);
                }
            });

            root.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClicked(day);
                }
            });
        }
    }
}
