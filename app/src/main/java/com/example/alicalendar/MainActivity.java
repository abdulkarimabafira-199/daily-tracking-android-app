package com.example.alicalendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMNS = 7;

    private Calendar visibleMonth;
    private TickStore tickStore;
    private CalendarAdapter adapter;

    private TextView tvMonthYear;
    private TextView tvDaysCounted;
    private TextView tvStreak;
    private TextView tvTodayDate;
    private RecyclerView rvDays;
    private DrawerLayout drawerLayout;
    private TextView tvMotivation;
    private android.widget.ProgressBar pbMonthlyProgress;
    private TextView tvProgressDetails;
    private TextView tvWinRate;
    private TextView tvStatCurrentStreak;
    private TextView tvStatBestStreak;
    private TextView tvStatTotal;
    private View vStatusDot;
    private TextView tvTodayStatusText;
    private LinearLayout llRecentActivity;
    private View llDailyInsight;
    private TextView tvInsightContent;
    private KonfettiView konfettiView;
    private final Handler motivationHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before onCreate, defaulting to Dark Mode
        int savedMode = getSharedPreferences("settings", MODE_PRIVATE)
                .getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_YES);
        AppCompatDelegate.setDefaultNightMode(savedMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tickStore = new TickStore(this);
        visibleMonth = Calendar.getInstance();
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvDaysCounted = findViewById(R.id.tvDaysCounted);
        tvStreak = findViewById(R.id.tvStreak);
        tvTodayDate = findViewById(R.id.tvTodayDate);
        rvDays = findViewById(R.id.rvDays);
        drawerLayout = findViewById(R.id.drawerLayout);
        tvMotivation = findViewById(R.id.tvMotivation);
        konfettiView = findViewById(R.id.konfettiView);
        pbMonthlyProgress = findViewById(R.id.pbMonthlyProgress);
        tvProgressDetails = findViewById(R.id.tvProgressDetails);
        tvWinRate = findViewById(R.id.tvWinRate);
        tvStatCurrentStreak = findViewById(R.id.tvStatCurrentStreak);
        tvStatBestStreak = findViewById(R.id.tvStatBestStreak);
        tvStatTotal = findViewById(R.id.tvStatTotal);
        vStatusDot = findViewById(R.id.vStatusDot);
        tvTodayStatusText = findViewById(R.id.tvTodayStatusText);
        llRecentActivity = findViewById(R.id.llRecentActivity);
        llDailyInsight = findViewById(R.id.llDailyInsight);
        tvInsightContent = findViewById(R.id.tvInsightContent);
        View llMonthPicker = findViewById(R.id.llMonthPicker);
        View llDaysCounted = findViewById(R.id.llDaysCounted);
        ImageButton btnMenu = findViewById(R.id.btnMenu);

        setTodayDate();

        btnMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        findViewById(R.id.navJumpToToday).setOnClickListener(v -> {
            jumpToToday();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.navViewHistory).setOnClickListener(v -> {
            showTickedDaysDialog();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        SwitchCompat themeSwitch = findViewById(R.id.themeSwitch);
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        themeSwitch.setChecked(isDarkMode);

        findViewById(R.id.navToggleTheme).setOnClickListener(v -> {
            boolean newDarkMode = !themeSwitch.isChecked();
            themeSwitch.setChecked(newDarkMode);
            int newMode = newDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            
            // Save preference
            getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putInt("theme_mode", newMode)
                .apply();
                
            AppCompatDelegate.setDefaultNightMode(newMode);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.navAbout).setOnClickListener(v -> {
            View aboutView = getLayoutInflater().inflate(R.layout.dialog_about, null);
            ImageButton btnTelegram = aboutView.findViewById(R.id.btnTelegram);
            ImageButton btnInstagram = aboutView.findViewById(R.id.btnInstagram);

            btnTelegram.setOnClickListener(v1 -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/A_Abafira"));
                startActivity(intent);
            });

            btnInstagram.setOnClickListener(v1 -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.instagram.com/a_abafira"));
                startActivity(intent);
            });

            new AlertDialog.Builder(this)
                .setView(aboutView)
                .setPositiveButton("OK", null)
                .show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.navEditName).setOnClickListener(v -> {
            showEditNameDialog();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.navReminderTime).setOnClickListener(v -> {
            showReminderTimePicker();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        ImageButton btnPrev = findViewById(R.id.btnPrevMonth);
        ImageButton btnNext = findViewById(R.id.btnNextMonth);

        rvDays.setLayoutManager(new GridLayoutManager(this, 7));

        // Enable horizontal swipe to change months using GestureDetector
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                
                // Ensure it's mostly a horizontal swipe
                if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 100 && Math.abs(velocityX) > 100) {
                    if (deltaX > 0) {
                        // Swipe Right -> Previous Month
                        visibleMonth.add(Calendar.MONTH, -1);
                    } else {
                        // Swipe Left -> Next Month
                        visibleMonth.add(Calendar.MONTH, 1);
                    }
                    refreshCalendar();
                    return true;
                }
                return false;
            }
        });

        rvDays.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false; // Let the RecyclerView or its children handle the actual touch/click
            }
        });

        btnPrev.setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, -1);
            refreshCalendar();
        });
        btnNext.setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, 1);
            refreshCalendar();
        });

        llMonthPicker.setOnClickListener(v -> showMonthYearPicker());
        tvTodayDate.setOnClickListener(v -> jumpToToday());
        llDaysCounted.setOnClickListener(v -> showTickedDaysDialog());

        refreshCalendar();
        playWelcomeAnimation();
        scheduleDailyReminder();
    }

    private void scheduleDailyReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Get saved reminder time
        int hour = tickStore.getReminderHour();
        int minute = tickStore.getReminderMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scheduleDailyReminder();
        }
    }

    private void jumpToToday() {
        visibleMonth = Calendar.getInstance();
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
        refreshCalendar();
    }

    private void showMonthYearPicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    visibleMonth.set(Calendar.YEAR, year);
                    visibleMonth.set(Calendar.MONTH, month);
                    visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
                    refreshCalendar();
                },
                visibleMonth.get(Calendar.YEAR),
                visibleMonth.get(Calendar.MONTH),
                visibleMonth.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTickedDaysDialog() {
        Set<String> tickedDates = tickStore.getAllTickedDates();
        if (tickedDates.isEmpty()) {
            Toast.makeText(this, R.string.no_days_counted, Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> sortedDates = new ArrayList<>(tickedDates);
        Collections.sort(sortedDates);
        Collections.reverse(sortedDates);

        int totalTicked = tickStore.getTotalTickedCount();
        int longestStreak = tickStore.getLongestStreak();
        int currentStreak = tickStore.getCurrentStreak();

        String stats = getString(R.string.stats_history_format, totalTicked, currentStreak, longestStreak);

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        ListView listView = new ListView(this);
        listView.setDividerHeight(1);

        TextView tvStats = new TextView(this);
        tvStats.setText(stats);
        tvStats.setTextSize(18);
        tvStats.setPadding(padding, padding, padding, padding);
        tvStats.setTextColor(getResources().getColor(R.color.text_dark));
        listView.addHeaderView(tvStats);

        final AlertDialog[] historyDialog = new AlertDialog[1];

        android.widget.BaseAdapter listAdapter = new android.widget.BaseAdapter() {
            @Override
            public int getCount() { return sortedDates.size(); }
            @Override
            public Object getItem(int position) { return sortedDates.get(position); }
            @Override
            public long getItemId(int position) { return position; }
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
                }
                
                convertView.setPadding(padding, padding, padding, padding);

                String dateKey = sortedDates.get(position);
                String dateDisplay;
                String subText;
                try {
                    java.util.Date date = inputFormat.parse(dateKey);
                    dateDisplay = (date != null) ? outputFormat.format(date) : dateKey;
                    long time = tickStore.getTickTime(dateKey);
                    subText = (time > 0) ? getString(R.string.checked_at, timeFormat.format(new java.util.Date(time))) : getString(R.string.time_not_recorded);
                    
                    String note = tickStore.getNote(dateKey);
                    if (note != null && !note.isEmpty()) {
                        subText = note + " • " + subText;
                    }
                } catch (Exception e) {
                    dateDisplay = dateKey;
                    subText = "";
                }

                TextView t1 = convertView.findViewById(android.R.id.text1);
                TextView t2 = convertView.findViewById(android.R.id.text2);
                t1.setText(dateDisplay);
                t2.setText(subText);
                
                t1.setTextSize(19);
                t2.setTextSize(15);
                t1.setTextColor(getResources().getColor(R.color.text_dark));
                t2.setTextColor(getResources().getColor(R.color.text_light));

                return convertView;
            }
        };

        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < listView.getHeaderViewsCount()) return;
            
            int actualPos = position - listView.getHeaderViewsCount();
            String dateKey = sortedDates.get(actualPos);
            String dateDisplay;
            try {
                java.util.Date date = inputFormat.parse(dateKey);
                dateDisplay = (date != null) ? outputFormat.format(date) : dateKey;
            } catch (Exception e) {
                dateDisplay = dateKey;
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.untick_dialog_title)
                    .setMessage(getString(R.string.untick_dialog_message, dateDisplay))
                    .setPositiveButton(R.string.untick_action, (d, w) -> {
                        tickStore.setTicked(dateKey, false);
                        refreshCalendar();
                        updateCounterBadge();
                        Toast.makeText(MainActivity.this, R.string.day_unticked, Toast.LENGTH_SHORT).show();
                        
                        if (historyDialog[0] != null) historyDialog[0].dismiss();
                        showTickedDaysDialog();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        historyDialog[0] = new AlertDialog.Builder(this)
                .setTitle(R.string.history_title)
                .setView(listView)
                .setPositiveButton(R.string.close, null)
                .show();
    }


    private void setTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(Calendar.getInstance().getTime());
        tvTodayDate.setText(currentDate);
    }

    /** Rebuilds the visible month's grid and the running tally badge. */
    private void refreshCalendar() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(monthFormat.format(visibleMonth.getTime()));

        List<CalendarDay> days = buildMonthGrid(visibleMonth);
        adapter = new CalendarAdapter(days, new CalendarAdapter.OnCalendarEventListener() {
            @Override
            public void onTickChanged(CalendarDay day, boolean isTicked) {
                if (!isTicked) {
                    // Ask for confirmation when unticking
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Remove Tick")
                            .setMessage("Are you sure you want to untick this day?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                tickStore.setTicked(day.getDateKey(), false);
                                updateCounterBadge();
                                updateMonthlyStats();
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                // Revert the change
                                day.setTicked(true);
                                refreshCalendar();
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    tickStore.setTicked(day.getDateKey(), true);
                    updateCounterBadge();
                    updateMonthlyStats();
                    showMotivationalMessage();
                    checkForMilestone();
                    
                    // Trigger haptic feedback
                    rvDays.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            }

            @Override
            public void onDayClicked(CalendarDay day) {
                showNoteDialog(day);
            }
        });
        rvDays.setAdapter(adapter);
        updateCounterBadge();
        updateMonthlyStats();
    }

    private void updateMonthlyStats() {
        if (pbMonthlyProgress == null) return;

        int year = visibleMonth.get(Calendar.YEAR);
        int month = visibleMonth.get(Calendar.MONTH);
        int tickedCount = tickStore.getTickedCountForMonth(year, month);
        int daysInMonth = visibleMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        pbMonthlyProgress.setMax(daysInMonth);
        pbMonthlyProgress.setProgress(tickedCount);

        tvProgressDetails.setText(getString(R.string.progress_details_format, tickedCount, daysInMonth));

        int percentage = (daysInMonth > 0) ? (tickedCount * 100 / daysInMonth) : 0;
        tvWinRate.setText(getString(R.string.percent_format, percentage));

        // Update Bottom Stats Row
        tvStatCurrentStreak.setText(String.valueOf(tickStore.getCurrentStreak()));
        tvStatBestStreak.setText(String.valueOf(tickStore.getLongestStreak()));
        tvStatTotal.setText(String.valueOf(tickStore.getTotalTickedCount()));

        updateTodayStatus();
        updateRecentActivity();
        updateDailyInsight();
    }

    private void updateDailyInsight() {
        if (llDailyInsight == null || tvInsightContent == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String todayKey = sdf.format(Calendar.getInstance().getTime());
        String note = tickStore.getNote(todayKey);

        if (note != null && !note.isEmpty()) {
            tvInsightContent.setText(note);
            tvInsightContent.setAlpha(1.0f);
        } else {
            tvInsightContent.setText(R.string.tap_to_add_note);
            tvInsightContent.setAlpha(0.5f);
        }

        llDailyInsight.setOnClickListener(v -> {
            // Find the CalendarDay for today in the current adapter
            Calendar today = Calendar.getInstance();
            CalendarDay todayDay = null;
            if (adapter != null) {
                for (CalendarDay day : adapter.getDays()) {
                    if (day.isCurrentMonth() && 
                        day.getDayOfMonth() == today.get(Calendar.DAY_OF_MONTH) &&
                        day.getMonth() == today.get(Calendar.MONTH) &&
                        day.getYear() == today.get(Calendar.YEAR)) {
                        todayDay = day;
                        break;
                    }
                }
            }

            if (todayDay != null) {
                showNoteDialog(todayDay);
            } else {
                // If today isn't in view, show a generic today note dialog
                showNoteDialogForDate(todayKey, "Today");
            }
        });
    }

    private void showNoteDialogForDate(String dateKey, String title) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_note, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText input = dialogView.findViewById(R.id.etNoteInput);

        tvTitle.setText(getString(R.string.add_notes_for, title));
        input.setText(tickStore.getNote(dateKey));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String note = input.getText().toString();
                    tickStore.setNote(dateKey, note);
                    updateDailyInsight();
                    refreshCalendar();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateRecentActivity() {
        if (llRecentActivity == null) return;
        llRecentActivity.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6); // Start from 6 days ago

        int size = (int) (16 * getResources().getDisplayMetrics().density);
        int margin = (int) (6 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < 7; i++) {
            String dateKey = sdf.format(cal.getTime());
            boolean isTicked = tickStore.isTicked(dateKey);

            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(lp);

            dot.setBackgroundResource(R.drawable.welcome_circle_soft);
            if (isTicked) {
                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.ali_green)));
                dot.setAlpha(1.0f);
            } else {
                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.text_muted)));
                dot.setAlpha(0.4f);
            }
            
            llRecentActivity.addView(dot);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void updateTodayStatus() {
        if (vStatusDot == null || tvTodayStatusText == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String todayKey = sdf.format(Calendar.getInstance().getTime());
        boolean isTicked = tickStore.isTicked(todayKey);

        if (isTicked) {
            vStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.ali_green)));
            tvTodayStatusText.setText(R.string.status_counted);
        } else {
            vStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.ali_coral)));
            tvTodayStatusText.setText(R.string.status_not_counted);
        }
    }

    private void showNoteDialog(CalendarDay day) {
        String title = day.getDayOfMonth() + " " + tvMonthYear.getText();
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_note, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText input = dialogView.findViewById(R.id.etNoteInput);
        
        tvTitle.setText(getString(R.string.add_notes_for, title));
        input.setText(day.getNote());
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (d, which) -> {
                    String note = input.getText().toString();
                    day.setNote(note);
                    tickStore.setNote(day.getDateKey(), note);
                    updateDailyInsight();
                    refreshCalendar();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
                
        dialog.show();
    }

    private void updateCounterBadge() {
        int total = tickStore.getTotalTickedCount();
        tvDaysCounted.setText(getString(R.string.days_counted_format, total));
        updateStreakBadge();
    }

    private void updateStreakBadge() {
        if (tvStreak == null) return;
        int streak = tickStore.getCurrentStreak();
        if (streak >= 2) {
            tvStreak.setVisibility(View.VISIBLE);
            tvStreak.setText(getString(R.string.streak_format, streak));
            if (streak % 5 == 0) {
                celebrateStreak(streak);
            }
        } else {
            tvStreak.setVisibility(View.GONE);
        }
    }

    private void celebrateStreak(int streak) {
        if (konfettiView == null) return;

        EmitterConfig emitterConfig = new Emitter(300, java.util.concurrent.TimeUnit.MILLISECONDS).max(100);
        Party party = new PartyFactory(emitterConfig)
                .shapes(Shape.Circle.INSTANCE, Shape.Square.INSTANCE)
                .spread(360)
                .position(0.5, 0.5)
                .sizes(new Size(8, 4f, 2f))
                .timeToLive(2000L)
                .colors(java.util.Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                .build();
        konfettiView.start(party);

        Toast.makeText(this, "Amazing! " + streak + " day streak!", Toast.LENGTH_SHORT).show();
    }

    private void checkForMilestone() {
        int total = tickStore.getTotalTickedCount();
        if (total > 0 && total % 10 == 0) {
            celebrateMilestone(total);
        }
    }

    private void celebrateMilestone(int total) {
        if (konfettiView == null) return;

        EmitterConfig emitterConfig = new Emitter(5, java.util.concurrent.TimeUnit.SECONDS).perSecond(30);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(10f, 30f)
                .timeToLive(2000L)
                .shapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .sizes(new Size(12, 5f, 0.2f))
                .position(0.0, 1.0, 1.0, 1.0)
                .colors(java.util.Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                .build();
        konfettiView.start(party);

        new AlertDialog.Builder(this)
                .setTitle("Milestone Reached!")
                .setMessage("Congratulations ! You have tracked " + total + " days.")
                .setPositiveButton("Awesome!", null)
                .show();
    }

    private void showMotivationalMessage() {
        if (tvMotivation == null) return;

        // Pick a random quote from resources
        String[] quotes = getResources().getStringArray(R.array.motivation_quotes);
        int index = new Random().nextInt(quotes.length);
        String quote = quotes[index];

        // If it's the "Great job!" quote (last one usually), format it with the name
        if (quote.contains("%1$s")) {
            quote = String.format(quote, getStoredName().split(" ")[0]);
        }
        tvMotivation.setText(quote);

        // Show with animation
        tvMotivation.setVisibility(View.VISIBLE);
        tvMotivation.setAlpha(0f);
        tvMotivation.setTranslationY(20f);
        tvMotivation.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start();

        // Hide after 3 seconds
        motivationHandler.removeCallbacksAndMessages(null);
        motivationHandler.postDelayed(() -> {
            tvMotivation.animate()
                    .alpha(0f)
                    .translationY(20f)
                    .setDuration(300)
                    .withEndAction(() -> tvMotivation.setVisibility(View.GONE))
                    .start();
        }, 3000);
    }

    private List<CalendarDay> buildMonthGrid(Calendar monthCursor) {
        List<CalendarDay> result = new ArrayList<>();

        Calendar cursor = (Calendar) monthCursor.clone();
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        int currentMonth = cursor.get(Calendar.MONTH);

        int leadingEmptyCells = cursor.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        cursor.add(Calendar.DAY_OF_MONTH, -leadingEmptyCells);

        // 6 rows x 7 columns always covers any month layout.
        int totalCells = 42;
        for (int i = 0; i < totalCells; i++) {
            boolean inCurrentMonth = cursor.get(Calendar.MONTH) == currentMonth;
            String dateKey = String.format(Locale.US, "%04d-%02d-%02d", 
                    cursor.get(Calendar.YEAR), cursor.get(Calendar.MONTH) + 1, cursor.get(Calendar.DAY_OF_MONTH));

            CalendarDay day = new CalendarDay(
                    cursor.get(Calendar.YEAR),
                    cursor.get(Calendar.MONTH),
                    cursor.get(Calendar.DAY_OF_MONTH),
                    inCurrentMonth,
                    inCurrentMonth && tickStore.isTicked(dateKey),
                    inCurrentMonth ? tickStore.getNote(dateKey) : "");
            result.add(day);

            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        return result;
    }

    private void playWelcomeAnimation() {
        FrameLayout overlay = findViewById(R.id.welcomeOverlay);
        FrameLayout badge = findViewById(R.id.welcomeBadge);
        TextView line1 = findViewById(R.id.tvWelcomeLine1);
        TextView line2 = findViewById(R.id.tvWelcomeLine2);
        TextView sub = findViewById(R.id.tvWelcomeSub);
        View llGuides = findViewById(R.id.llGuides);
        View btnOk = findViewById(R.id.btnWelcomeOk);
        
        line2.setText(getStoredName());
        View circleOne = findViewById(R.id.circleOne);
        View circleTwo = findViewById(R.id.circleTwo);
        View circleThree = findViewById(R.id.circleThree);

        // Start states.
        badge.setScaleX(0f);
        badge.setScaleY(0f);
        badge.setAlpha(0f);
        line1.setTranslationY(24f);
        line2.setTranslationY(24f);
        sub.setTranslationY(16f);
        llGuides.setTranslationY(20f);
        btnOk.setTranslationY(20f);
        circleOne.setAlpha(0f);
        circleTwo.setAlpha(0f);
        circleThree.setAlpha(0f);

        // Badge pops in with a slight overshoot.
        ObjectAnimator badgeScaleX = ObjectAnimator.ofFloat(badge, View.SCALE_X, 0f, 1f);
        ObjectAnimator badgeScaleY = ObjectAnimator.ofFloat(badge, View.SCALE_Y, 0f, 1f);
        ObjectAnimator badgeAlpha = ObjectAnimator.ofFloat(badge, View.ALPHA, 0f, 1f);
        AnimatorSet badgeIn = new AnimatorSet();
        badgeIn.setInterpolator(new OvershootInterpolator(2.2f));
        badgeIn.setDuration(520);
        badgeIn.playTogether(badgeScaleX, badgeScaleY, badgeAlpha);

        // Floating decoration circles drift in softly, slightly staggered.
        AnimatorSet circlesIn = new AnimatorSet();
        circlesIn.playTogether(
                ObjectAnimator.ofFloat(circleOne, View.ALPHA, 0f, 1f).setDuration(700),
                ObjectAnimator.ofFloat(circleTwo, View.ALPHA, 0f, 1f).setDuration(700),
                ObjectAnimator.ofFloat(circleThree, View.ALPHA, 0f, 1f).setDuration(700));

        // "Welcome" line rises and fades in.
        AnimatorSet line1In = new AnimatorSet();
        line1In.setDuration(420);
        line1In.playTogether(
                ObjectAnimator.ofFloat(line1, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(line1, View.TRANSLATION_Y, 24f, 0f));

        // "Ali Hussein" line rises and fades in right after.
        AnimatorSet line2In = new AnimatorSet();
        line2In.setDuration(460);
        line2In.playTogether(
                ObjectAnimator.ofFloat(line2, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(line2, View.TRANSLATION_Y, 24f, 0f));

        // Subtitle eases in.
        AnimatorSet subIn = new AnimatorSet();
        subIn.setDuration(380);
        subIn.playTogether(
                ObjectAnimator.ofFloat(sub, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(sub, View.TRANSLATION_Y, 16f, 0f));

        // Guides and Button fade in.
        AnimatorSet guidesIn = new AnimatorSet();
        guidesIn.setDuration(500);
        guidesIn.playTogether(
                ObjectAnimator.ofFloat(llGuides, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(llGuides, View.TRANSLATION_Y, 20f, 0f),
                ObjectAnimator.ofFloat(btnOk, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(btnOk, View.TRANSLATION_Y, 20f, 0f));

        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(circlesIn, badgeIn);
        entrance.play(line1In).after(badgeIn);
        entrance.play(line2In).after(line1In);
        entrance.play(subIn).after(line2In);
        entrance.play(guidesIn).after(subIn);

        btnOk.setOnClickListener(v -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(overlay, View.ALPHA, 1f, 0f);
            fadeOut.setDuration(500);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    overlay.setVisibility(View.GONE);
                }
            });
            fadeOut.start();
        });

        entrance.start();
    }

    private String getStoredName() {
        return getSharedPreferences("settings", MODE_PRIVATE)
                .getString("user_name", getString(R.string.default_name));
    }

    private void showReminderTimePicker() {
        int currentHour = tickStore.getReminderHour();
        int currentMinute = tickStore.getReminderMinute();

        new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
            tickStore.setReminderTime(hourOfDay, minute);
            scheduleDailyReminder();
            Toast.makeText(this, R.string.reminder_time_updated, Toast.LENGTH_SHORT).show();
        }, currentHour, currentMinute, false).show();
    }

    private void showEditNameDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_note, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText input = dialogView.findViewById(R.id.etNoteInput);
        
        tvTitle.setText(R.string.edit_name_title);
        input.setText(getStoredName());
        input.setHint(""); // No hint needed for name
        input.setMinLines(1);
        
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        getSharedPreferences("settings", MODE_PRIVATE)
                                .edit()
                                .putString("user_name", newName)
                                .apply();
                        Toast.makeText(this, R.string.name_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
