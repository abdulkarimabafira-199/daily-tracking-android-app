package com.example.alicalendar;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class AliCalendarApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize theme from preferences, defaulting to Dark Mode if not set
        int savedMode = getSharedPreferences("settings", MODE_PRIVATE)
                .getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_YES);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }
}
