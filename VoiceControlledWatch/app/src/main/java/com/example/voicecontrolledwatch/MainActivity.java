package com.example.voicecontrolledwatch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.voicecontrolledwatch.R;
import com.example.voicecontrolledwatch.modules.StopwatchModule;
import com.example.voicecontrolledwatch.modules.TimerModule;
import com.example.voicecontrolledwatch.utils.Constants;
import com.example.voicecontrolledwatch.voice.VoiceCommandProcessor;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements VoiceCommandProcessor.CommandListener {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 100;
    private static final String TRIGGER_PHRASE = "hey watch";  // Customizable trigger phrase

    private ViewFlipper viewFlipper;
    private Button toggleButton;
    private Button voiceCommandButton;
    private Button triggerModeButton;
    private TextView statusText;

    private StopwatchModule stopwatchModule;
    private TimerModule timerModule;
    private VoiceCommandProcessor voiceCommandProcessor;

    private boolean isListening = false;
    private boolean triggerModeActive = false;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        viewFlipper = findViewById(R.id.view_flipper);
        toggleButton = findViewById(R.id.toggle_button);
        voiceCommandButton = findViewById(R.id.voice_command_button);
        triggerModeButton = findViewById(R.id.trigger_mode_button);
        statusText = findViewById(R.id.status_text);

        // Initialize modules
        stopwatchModule = new StopwatchModule(this);
        timerModule = new TimerModule(this);

        // Initialize voice command processor
        voiceCommandProcessor = new VoiceCommandProcessor(this);

        // Setup speech recognizer
        setupSpeechRecognizer();

        // Set up toggle button to switch between stopwatch and timer
        toggleButton.setOnClickListener(v -> toggleModule());

        // Set up voice command button
        voiceCommandButton.setOnClickListener(v -> toggleVoiceRecognition());

        // Set up trigger mode button
        triggerModeButton.setOnClickListener(v -> toggleTriggerMode());

        // Check for audio recording permission
        checkAudioPermission();
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                if (!triggerModeActive) {
                    statusText.setText(R.string.listening);
                }
            }

            @Override
            public void onBeginningOfSpeech() {
                // Not needed for this implementation
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Not needed for this implementation
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Not needed for this implementation
            }

            @Override
            public void onEndOfSpeech() {
                if (!triggerModeActive) {
                    statusText.setText(R.string.processing);
                    isListening = false;
                    voiceCommandButton.setText(R.string.start_listening);
                }
            }

            @Override
            public void onError(int error) {
                if (triggerModeActive) {
                    // In trigger mode, just restart listening on error
                    startListeningWithTrigger();
                } else {
                    isListening = false;
                    voiceCommandButton.setText(R.string.start_listening);
                    statusText.setText(R.string.error_listening);
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0).toLowerCase();
                    statusText.setText(getString(R.string.recognized_command, recognizedText));

                    // Process the command
                    voiceCommandProcessor.processCommand(recognizedText);

                    // If in trigger mode, start listening again
                    if (triggerModeActive) {
                        startListeningWithTrigger();
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Not needed for this implementation
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Not needed for this implementation
            }
        });
    }

    private void toggleVoiceRecognition() {
        // If trigger mode is active, disable it
        if (triggerModeActive) {
            stopTriggerMode();
        }

        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            voiceCommandButton.setText(R.string.start_listening);
        } else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecognizer.startListening(intent);
            isListening = true;
            voiceCommandButton.setText(R.string.stop_listening);
        }
    }

    private void startListeningWithTrigger() {
        // For continuous command listening in trigger mode
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        // Add a short delay to avoid rapid consecutive recognitions
        new android.os.Handler().postDelayed(() -> {
            if (triggerModeActive) {
                speechRecognizer.startListening(intent);
                statusText.setText(R.string.trigger_mode_active);
            }
        }, 500);
    }

    private void stopTriggerMode() {
        if (triggerModeActive) {
            triggerModeActive = false;
            speechRecognizer.stopListening();
            statusText.setText(R.string.trigger_mode_off);
            triggerModeButton.setText(R.string.enable_trigger_mode);
        }
    }

    private void toggleTriggerMode() {
        if (triggerModeActive) {
            stopTriggerMode();
        } else {
            // If manual listening is active, stop it first
            if (isListening) {
                speechRecognizer.stopListening();
                isListening = false;
                voiceCommandButton.setText(R.string.start_listening);
            }

            triggerModeActive = true;
            triggerModeButton.setText(R.string.disable_trigger_mode);
            statusText.setText(R.string.trigger_mode_active);

            // Start listening
            startListeningWithTrigger();
        }
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause listening when app is in background
        if (triggerModeActive) {
            speechRecognizer.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume listening when app is back in foreground
        if (triggerModeActive) {
            startListeningWithTrigger();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop trigger mode if active
        if (triggerModeActive) {
            stopTriggerMode();
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        // Cleanup modules
        stopwatchModule.cleanup();
        timerModule.cleanup();
    }

    private void toggleModule() {
        // Switch between stopwatch and timer views
        if (viewFlipper.getDisplayedChild() == 0) {
            // Switch to timer
            viewFlipper.showNext();
            toggleButton.setText(R.string.switch_to_stopwatch);
        } else {
            // Switch to stopwatch
            viewFlipper.showPrevious();
            toggleButton.setText(R.string.switch_to_timer);
        }
    }

    // Method to get the current active module
    public boolean isStopwatchActive() {
        return viewFlipper.getDisplayedChild() == 0;
    }

    // VoiceCommandProcessor.CommandListener implementations
    @Override
    public void onStartCommand() {
        if (isStopwatchActive()) {
            stopwatchModule.start();
        } else {
            timerModule.start();
        }
    }

    @Override
    public void onStopCommand() {
        if (isStopwatchActive()) {
            stopwatchModule.stop();
        } else {
            timerModule.stop();
        }
    }

    @Override
    public void onResetCommand() {
        if (isStopwatchActive()) {
            stopwatchModule.reset();
        } else {
            timerModule.reset();
        }
    }

    @Override
    public void onSwitchCommand() {
        toggleModule();
    }

    @Override
    public void onSetTimerCommand(int minutes, int seconds) {
        if (!isStopwatchActive()) {
            timerModule.setTime(minutes, seconds);
        } else {
            // Switch to timer first
            toggleModule();
            timerModule.setTime(minutes, seconds);
        }
    }
}