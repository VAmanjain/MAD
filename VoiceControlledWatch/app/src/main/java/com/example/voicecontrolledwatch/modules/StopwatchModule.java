package com.example.voicecontrolledwatch.modules;

import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import com.example.voicecontrolledwatch.R;
import com.example.voicecontrolledwatch.MainActivity;

import java.util.Locale;

public class StopwatchModule {
    private static final int INTERVAL_MS = 10;  // Update every 10 milliseconds

    private final MainActivity activity;
    private final Handler handler;

    private TextView timeDisplay;
    private Button startStopButton;
    private Button resetButton;

    private long startTime = 0L;
    private long elapsedTime = 0L;
    private boolean isRunning = false;

    private final Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long currentTime = System.currentTimeMillis();
                elapsedTime = currentTime - startTime;
                updateDisplay();
                handler.postDelayed(this, INTERVAL_MS);
            }
        }
    };

    public StopwatchModule(MainActivity activity) {
        this.activity = activity;
        this.handler = new Handler(Looper.getMainLooper());

        // Initialize UI components
        initializeUI();

        // Set initial display
        updateDisplay();
    }

    private void initializeUI() {
        timeDisplay = activity.findViewById(R.id.stopwatch_time_display);
        startStopButton = activity.findViewById(R.id.stopwatch_start_stop_button);
        resetButton = activity.findViewById(R.id.stopwatch_reset_button);

        // Set up manual control buttons
        startStopButton.setOnClickListener(v -> {
            if (isRunning) {
                stop();
            } else {
                start();
            }
        });

        resetButton.setOnClickListener(v -> reset());
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            startStopButton.setText(R.string.stop);

            if (elapsedTime == 0) {
                // Fresh start
                startTime = System.currentTimeMillis();
            } else {
                // Resume from where we left off
                startTime = System.currentTimeMillis() - elapsedTime;
            }

            handler.post(timeUpdater);
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            startStopButton.setText(R.string.start);
            handler.removeCallbacks(timeUpdater);
        }
    }

    public void reset() {
        stop();
        elapsedTime = 0L;
        updateDisplay();
    }

    private void updateDisplay() {
        // Format time as mm:ss.ms
        long minutes = (elapsedTime / 1000) / 60;
        long seconds = (elapsedTime / 1000) % 60;
        long milliseconds = (elapsedTime % 1000) / 10;  // Show only centiseconds

        String formattedTime = String.format(Locale.getDefault(),
                "%02d:%02d.%02d", minutes, seconds, milliseconds);
        timeDisplay.setText(formattedTime);
    }

    public void cleanup() {
        handler.removeCallbacks(timeUpdater);
    }

    public boolean isRunning() {
        return isRunning;
    }
}