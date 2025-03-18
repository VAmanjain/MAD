package com.example.voicecontrolledwatch.voice;

import com.example.voicecontrolledwatch.MainActivity;
import com.example.voicecontrolledwatch.utils.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceCommandProcessor {
    // Interface for command callbacks
    public interface CommandListener {
        void onStartCommand();
        void onStopCommand();
        void onResetCommand();
        void onSwitchCommand();
        void onSetTimerCommand(int minutes, int seconds);
    }

    private final CommandListener listener;

    // Regex pattern for timer commands like "set timer for 2 minutes and 30 seconds"
    private static final Pattern TIMER_PATTERN = Pattern.compile(
            "(?:set|start)\\s+timer\\s+(?:for|to)?\\s+(?:(\\d+)\\s+minute(?:s)?)?\\s*(?:and)?\\s*(?:(\\d+)\\s+second(?:s)?)?",
            Pattern.CASE_INSENSITIVE);

    public VoiceCommandProcessor(CommandListener listener) {
        this.listener = listener;
    }

    public void processCommand(String command) {
        // Convert command to lowercase for easier matching
        String lowercaseCommand = command.trim().toLowerCase();

        // Check for timer set command first (most complex)
        Matcher timerMatcher = TIMER_PATTERN.matcher(lowercaseCommand);
        if (timerMatcher.find()) {
            int minutes = 0;
            int seconds = 0;

            String minutesStr = timerMatcher.group(1);
            String secondsStr = timerMatcher.group(2);

            if (minutesStr != null && !minutesStr.isEmpty()) {
                minutes = Integer.parseInt(minutesStr);
            }

            if (secondsStr != null && !secondsStr.isEmpty()) {
                seconds = Integer.parseInt(secondsStr);
            }

            // Ensure we have at least some time set
            if (minutes > 0 || seconds > 0) {
                listener.onSetTimerCommand(minutes, seconds);
                return;
            }
        }

        // Check for other commands
        if (containsAny(lowercaseCommand, Constants.START_COMMANDS)) {
            listener.onStartCommand();
        } else if (containsAny(lowercaseCommand, Constants.STOP_COMMANDS)) {
            listener.onStopCommand();
        } else if (containsAny(lowercaseCommand, Constants.RESET_COMMANDS)) {
            listener.onResetCommand();
        } else if (containsAny(lowercaseCommand, Constants.SWITCH_COMMANDS)) {
            listener.onSwitchCommand();
        }
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}