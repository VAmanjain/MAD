package com.example.voicecontrolledwatch.modules;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.voicecontrolledwatch.R;
import com.example.voicecontrolledwatch.MainActivity;

import java.util.Locale;

public class TimerModule {
    private final MainActivity activity;

    private TextView timeDisplay;
    private Button startStopButton;
    private Button resetButton;
    private Button increaseButton;
    private Button decreaseButton;

    private long totalTimeInMillis = 0L;  // Default timer duration
    private long remainingTimeInMillis = 0L;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private MediaPlayer alarmSound;

    public TimerModule(MainActivity activity) {
        this.activity = activity;

        // Initialize UI components
        initializeUI();

        // Initialize alarm sound
        alarmSound = MediaPlayer.create(activity, R.raw.alarm);

        // Set initial display
        updateDisplay();
    }

    private void initializeUI() {
        timeDisplay = activity.findViewById(R.id.timer_time_display);
        startStopButton = activity.findViewById(R.id.timer_start_stop_button);
        resetButton = activity.findViewById(R.id.timer_reset_button);
        increaseButton = activity.findViewById(R.id.timer_increase_button);
        decreaseButton = activity.findViewById(R.id.timer_decrease_button);

        // Set up manual control buttons
        startStopButton.setOnClickListener(v -> {
            if (isRunning) {
                stop();
            } else {
                start();
            }
        });

        resetButton.setOnClickListener(v -> reset());

        increaseButton.setOnClickListener(v -> {
            if (!isRunning) {
                setTime(0, 30, true);  // Add 30 seconds
            }
        });

        decreaseButton.setOnClickListener(v -> {
            if (!isRunning) {
                setTime(0, -30, true);  // Subtract 30 seconds
            }
        });
    }

    public void setTime(int minutes, int seconds) {
        setTime(minutes, seconds, false);
    }

    private void setTime(int minutes, int seconds, boolean isRelative) {
        if (isRunning) {
            stop();
        }

        if (isRelative) {
            // Calculate new time by adding to current time
            long newTimeInMillis = totalTimeInMillis + (minutes * 60 * 1000L) + (seconds * 1000L);
            totalTimeInMillis = Math.max(0, newTimeInMillis);  // Ensure non-negative
        } else {
            // Set absolute time
            totalTimeInMillis = (minutes * 60 * 1000L) + (seconds * 1000L);
        }

        remainingTimeInMillis = totalTimeInMillis;
        updateDisplay();
    }

    public void start() {
        if (!isRunning && remainingTimeInMillis > 0) {
            isRunning = true;
            startStopButton.setText(R.string.stop);

            countDownTimer = new CountDownTimer(remainingTimeInMillis, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    remainingTimeInMillis = millisUntilFinished;
                    updateDisplay();
                }

                @Override
                public void onFinish() {
                    isRunning = false;
                    remainingTimeInMillis = 0;
                    updateDisplay();
                    startStopButton.setText(R.string.start);

                    // Play alarm sound
                    playAlarm();

                    // Display message
                    Toast.makeText(activity, R.string.timer_finished, Toast.LENGTH_SHORT).show();
                }
            }.start();
        } else if (remainingTimeInMillis <= 0) {
            // Notify user to set time first
            Toast.makeText(activity, R.string.set_time_first, Toast.LENGTH_SHORT).show();
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            startStopButton.setText(R.string.start);

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        }
    }

    public void reset() {
        stop();
        remainingTimeInMillis = totalTimeInMillis;
        updateDisplay();
    }

    private void updateDisplay() {
        // Format time as mm:ss
        long minutes = (remainingTimeInMillis / 1000) / 60;
        long seconds = (remainingTimeInMillis / 1000) % 60;

        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timeDisplay.setText(formattedTime);
    }

    private void playAlarm() {
        if (alarmSound != null) {
            alarmSound.start();
        }
    }

    public void cleanup() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (alarmSound != null) {
            alarmSound.release();
            alarmSound = null;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}