package com.example.voicecontrolledwatch.utils;

public class Constants {
    // Voice command keywords
    public static final String[] START_COMMANDS = {
            "start", "begin", "go", "resume","chal", "shuru kar", "aarambh", "wapas", "wapas shuru", "wapas shuru kar", "wapas chalu kr"
    };

    public static final String[] STOP_COMMANDS = {
            "stop", "pause", "halt", "freeze", "ruk", "rukja", "viram"
    };

    public static final String[] RESET_COMMANDS = {
            "reset", "clear", "restart", "zero","saaf", "punah aarambh"
    };

    public static final String[] SWITCH_COMMANDS = {
            "switch", "toggle", "change", "flip",
            "stopwatch", "timer",
            "change to stopwatch", "change to timer",
            "switch to stopwatch", "switch to timer",
            "badal", "rupantaran"
    };

    // Command types
    public static final String COMMAND_START = "start";
    public static final String COMMAND_STOP = "stop";
    public static final String COMMAND_RESET = "reset";
    public static final String COMMAND_SWITCH = "switch";
    public static final String COMMAND_SET_TIMER = "set_timer";

    // Module identifiers
    public static final int MODULE_STOPWATCH = 0;
    public static final int MODULE_TIMER = 1;

    // Timer constants
    public static final long DEFAULT_TIMER_DURATION_MS = 60 * 1000;  // 1 minute
}