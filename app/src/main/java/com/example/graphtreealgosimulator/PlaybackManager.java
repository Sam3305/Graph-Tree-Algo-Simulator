package com.example.graphtreealgosimulator;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;


import com.example.graphtreealgosimulator.view.GraphView;

import java.util.ArrayList;
import java.util.List;

public class PlaybackManager {

    private final Activity activity;
    private final GraphView graphView;
    private final TerminalManager terminalManager;

    private SeekBar timelineSlider;
    private ImageButton btnPlayAlgorithm;

    private Handler playbackHandler = new Handler(Looper.getMainLooper());
    private List<Runnable> currentAnimationSteps = new ArrayList<>();
    private int currentStepIndex = 0;
    private boolean isPaused = false;
    private boolean isAlgorithmRunning = false;

    public PlaybackManager(Activity activity, GraphView graphView, TerminalManager terminalManager) {
        this.activity = activity;
        this.graphView = graphView;
        this.terminalManager = terminalManager;

        timelineSlider = activity.findViewById(R.id.timeline_slider);
        btnPlayAlgorithm = activity.findViewById(R.id.btn_play_algorithm);

        setupSlider();
    }

    private void setupSlider() {
        timelineSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentStepIndex = progress;
                    renderStateAt(currentStepIndex);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPaused = true; // Auto-pause when grabbing the slider
                btnPlayAlgorithm.setBackground(activity.getDrawable(R.drawable.pause_button));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    public boolean isRunning() {
        return isAlgorithmRunning;
    }

    public void togglePausePlay() {
        isPaused = !isPaused;
        if (isPaused) {
            btnPlayAlgorithm.setBackground(activity.getDrawable(R.drawable.play_button));
            terminalManager.setActionText("Paused...");
            playbackHandler.removeCallbacksAndMessages(null);
        } else {
            btnPlayAlgorithm.setBackground(activity.getDrawable(R.drawable.pause_button));
            executeNextStep();
        }
    }

    public void startPlayback(List<Runnable> steps) {
        this.currentAnimationSteps = steps;
        this.currentStepIndex = 0;
        this.isPaused = false;
        this.isAlgorithmRunning = true;

        btnPlayAlgorithm.setBackground(activity.getDrawable(R.drawable.pause_button));
        timelineSlider.setVisibility(View.VISIBLE);
        timelineSlider.setMax(steps.size() - 1);
        timelineSlider.setProgress(0);

        executeNextStep();
    }

    private void executeNextStep() {
        if (isPaused || !isAlgorithmRunning) return;

        if (currentStepIndex < currentAnimationSteps.size()) {
            renderStateAt(currentStepIndex);
            currentStepIndex++;
            playbackHandler.postDelayed(this::executeNextStep, 1000); // 1 second per frame
        } else {

            isAlgorithmRunning = false;
            playbackHandler.removeCallbacksAndMessages(null);

            // Notice we do NOT hide the timelineSlider here!
            btnPlayAlgorithm.setBackground(activity.getDrawable(R.drawable.play_button)); // Resets the button so they can replay it

            terminalManager.setActionText("Algorithm Complete!");
            graphView.setCurrentNode(null);
            graphView.frameTraversalResult(); // Trigger cinematic camera
        }
    }

    private void renderStateAt(int targetIndex) {
        if (currentAnimationSteps == null || currentAnimationSteps.isEmpty()) return;

        graphView.resetTraversal();
        terminalManager.clear();
        terminalManager.setStatusText("Output: ");

        for (int i = 0; i <= targetIndex; i++) {
            currentAnimationSteps.get(i).run();
        }

        timelineSlider.setProgress(targetIndex);
    }

    public void stopEngine() {
        isAlgorithmRunning = false;
        isPaused = false;
        playbackHandler.removeCallbacksAndMessages(null);
        if (timelineSlider != null) timelineSlider.setVisibility(View.GONE);
        if (btnPlayAlgorithm != null) btnPlayAlgorithm.setBackground(activity.getDrawable(R.drawable.play_button));
    }
}