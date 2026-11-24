package com.example.alicalendar;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Persists which dates the user has ticked off, so the "prisoner style"
 * day count survives app restarts. Backed by a single SharedPreferences
 * StringSet keyed by date (yyyy-MM-dd).
 */
public class TickStore {

    private static final String PREFS_NAME = "ali_calendar_prefs";
    private static final String KEY_TICKED_DATES = "ticked_dates";
    private static final String PREFIX_NOTE = "note_";
    private static final String PREFIX_TIME = "tick_time_";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";

    private final SharedPreferences prefs;

    public TickStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isTicked(String dateKey) {
        return getTickedDates().contains(dateKey);
    }

    public void setTicked(String dateKey, boolean ticked) {
        Set<String> updated = getTickedDates();
        SharedPreferences.Editor editor = prefs.edit();
        if (ticked) {
            updated.add(dateKey);
            // Record current time if not already recorded
            if (!prefs.contains(PREFIX_TIME + dateKey)) {
                editor.putLong(PREFIX_TIME + dateKey, System.currentTimeMillis());
            }
        } else {
            updated.remove(dateKey);
            editor.remove(PREFIX_TIME + dateKey);
        }
        editor.putStringSet(KEY_TICKED_DATES, updated).apply();
    }

    public long getTickTime(String dateKey) {
        return prefs.getLong(PREFIX_TIME + dateKey, 0);
    }

    public String getNote(String dateKey) {
        return prefs.getString(PREFIX_NOTE + dateKey, "");
    }

    public void setNote(String dateKey, String note) {
        prefs.edit().putString(PREFIX_NOTE + dateKey, note).apply();
    }

    public int getReminderHour() {
        return prefs.getInt(KEY_REMINDER_HOUR, 20); // Default 8 PM
    }

    public int getReminderMinute() {
        return prefs.getInt(KEY_REMINDER_MINUTE, 0);
    }

    public void setReminderTime(int hour, int minute) {
        prefs.edit()
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .apply();
    }

    /** Total number of days ever ticked - the running "prisoner tally". */
    public int getTotalTickedCount() {
        return getTickedDates().size();
    }

    public int getTickedCountForMonth(int year, int month) {
        Set<String> tickedDates = getTickedDates();
        int count = 0;
        String prefix = String.format(Locale.US, "%04d-%02d-", year, month + 1);
        for (String date : tickedDates) {
            if (date.startsWith(prefix)) {
                count++;
            }
        }
        return count;
    }

    public Set<String> getAllTickedDates() {
        return getTickedDates();
    }

    /** Calculates the current consecutive days streak ending today or yesterday. */
    /** Calculates the current consecutive days streak ending today or yesterday. */
    public int getCurrentStreak() {
        Set<String> tickedDates = getTickedDates();
        if (tickedDates.isEmpty()) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        String todayKey = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayKey = sdf.format(cal.getTime());

        // Streak is broken if neither today nor yesterday is ticked
        if (!tickedDates.contains(todayKey) && !tickedDates.contains(yesterdayKey)) {
            return 0;
        }

        int streak = 0;
        cal = Calendar.getInstance(); // Reset to today

        // If today isn't ticked, start checking from yesterday
        if (!tickedDates.contains(todayKey)) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        while (tickedDates.contains(sdf.format(cal.getTime()))) {
            streak++;
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        return streak;
    }

    public int getLongestStreak() {
        Set<String> tickedDates = getTickedDates();
        if (tickedDates.isEmpty()) return 0;

        List<String> sortedDates = new ArrayList<>(tickedDates);
        Collections.sort(sortedDates);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        int maxStreak = 0;
        int currentStreak = 0;
        Calendar lastDate = null;

        for (String dateStr : sortedDates) {
            try {
                Calendar current = Calendar.getInstance();
                current.setTime(sdf.parse(dateStr));

                if (lastDate == null) {
                    currentStreak = 1;
                } else {
                    lastDate.add(Calendar.DAY_OF_YEAR, 1);
                    if (sdf.format(lastDate.getTime()).equals(sdf.format(current.getTime()))) {
                        currentStreak++;
                    } else {
                        currentStreak = 1;
                    }
                    lastDate.add(Calendar.DAY_OF_YEAR, -1); // reset after check
                }
                maxStreak = Math.max(maxStreak, currentStreak);
                lastDate = current;
            } catch (Exception e) {
                // ignore
            }
        }
        return maxStreak;
    }

    private Set<String> getTickedDates() {
        // Defensive copy: SharedPreferences returns a live-ish set that
        // should never be mutated directly.
        Set<String> stored = prefs.getStringSet(KEY_TICKED_DATES, new HashSet<>());
        return new HashSet<>(stored);
    }
}
