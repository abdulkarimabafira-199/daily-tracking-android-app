package com.example.alicalendar;

import java.util.Calendar;
import java.util.Locale;

/**
 * A single cell in the calendar grid. Days that fall outside the
 * currently displayed month are still modeled so the grid stays
 * perfectly aligned, but they are rendered as empty placeholders.
 */
public class CalendarDay {

    private final int year;
    private final int month; // 0-based, matches java.util.Calendar
    private final int dayOfMonth;
    private final boolean currentMonth;
    private boolean ticked;
    private String note;

    public CalendarDay(int year, int month, int dayOfMonth, boolean currentMonth, boolean ticked, String note) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.currentMonth = currentMonth;
        this.ticked = ticked;
        this.note = note;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isCurrentMonth() {
        return currentMonth;
    }

    public boolean isTicked() {
        return ticked;
    }

    public void setTicked(boolean ticked) {
        this.ticked = ticked;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /** Stable key used for persisting the tick state, e.g. "2026-07-18". */
    public String getDateKey() {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
    }

    public boolean isToday() {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == year
                && today.get(Calendar.MONTH) == month
                && today.get(Calendar.DAY_OF_MONTH) == dayOfMonth;
    }

    public boolean isWeekend() {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, dayOfMonth);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }
}
