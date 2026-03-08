package com.example.graphtreealgosimulator;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.GraphType;
import com.example.graphtreealgosimulator.view.GraphView;
import com.example.graphtreealgosimulator.view.InteractionMode;

public class UIManager {

    private final Activity activity;
    private final GraphView graphView;
    private final Graph graph;

    // Graph Type Variables
    private View viewThumb;
    private TextView tvDirected, tvUndirected;
    private boolean isDirectedSelected = true;

    // Dropdown Variables
    private LinearLayout dropdownHeader, dropdownOptions;
    private ImageView dropdownArrow;
    private TextView tvSelectedAlgorithm, optionBfs, optionDfs, optionDijkstra, optionAstar, optionUcs, optionDls, optionBidirectional;
    private boolean isDropdownExpanded = false;
    private String currentSelectedAlgorithm = "";

    // Sidebar Variables
    private TextView toolNode,toolStartNode, toolGoalNode;
    private ImageButton toolEdge, toolEraser;

    public UIManager(Activity activity, GraphView graphView, Graph graph) {
        this.activity = activity;
        this.graphView = graphView;
        this.graph = graph;

        initViews();
        setupListeners();

        // Highlight the default tool on startup
        selectTool(InteractionMode.ADD_NODE);
    }

    private void initViews() {
        // Find all the views automatically!
        viewThumb = activity.findViewById(R.id.view_thumb);
        tvDirected = activity.findViewById(R.id.tv_directed);
        tvUndirected = activity.findViewById(R.id.tv_undirected);

        dropdownHeader = activity.findViewById(R.id.dropdown_header);
        dropdownOptions = activity.findViewById(R.id.dropdown_options);
        dropdownArrow = activity.findViewById(R.id.dropdown_arrow);
        tvSelectedAlgorithm = activity.findViewById(R.id.tv_selected_algorithm);

        optionBfs = activity.findViewById(R.id.option_bfs);
        optionDfs = activity.findViewById(R.id.option_dfs);
        optionDijkstra = activity.findViewById(R.id.option_dijkstra);
        optionAstar = activity.findViewById(R.id.option_astar);
        optionUcs = activity.findViewById(R.id.option_ucs);
        optionDls = activity.findViewById(R.id.option_dls);
        optionBidirectional = activity.findViewById(R.id.option_bidirectional);

        // --- HIDE BIDIRECTIONAL SEARCH FOR NOW ---
        // This makes the button disappear from the dropdown, but keeps all our code safe!
        optionBidirectional.setVisibility(View.GONE);

        toolNode = activity.findViewById(R.id.tool_node);
        toolEdge = activity.findViewById(R.id.tool_edge);
        toolStartNode = activity.findViewById(R.id.tool_start_node);
        toolGoalNode = activity.findViewById(R.id.tool_goal_node);
        toolEraser = activity.findViewById(R.id.tool_eraser);
    }

    private void setupListeners() {
        // Attach all the click logic
        tvDirected.setOnClickListener(v -> selectDirected());
        tvUndirected.setOnClickListener(v -> selectUndirected());

        dropdownHeader.setOnClickListener(v -> toggleDropdown());
        optionBfs.setOnClickListener(v -> handleOptionSelected("BFS"));
        optionDfs.setOnClickListener(v -> handleOptionSelected("DFS"));
        optionDijkstra.setOnClickListener(v -> handleOptionSelected("Dijkstra"));
        optionAstar.setOnClickListener(v -> handleOptionSelected("A*"));
        optionUcs.setOnClickListener(v -> handleOptionSelected("UCS"));
        optionDls.setOnClickListener(v -> handleOptionSelected("DLS"));
        optionBidirectional.setOnClickListener(v -> handleOptionSelected("Bidirectional"));



        // We use GraphView's built-in InteractionMode directly now!
        toolNode.setOnClickListener(v -> selectTool(InteractionMode.ADD_NODE));
        toolEdge.setOnClickListener(v -> selectTool(InteractionMode.ADD_EDGE));
        toolStartNode.setOnClickListener(v -> selectTool(InteractionMode.SELECT_START_NODE));
        toolGoalNode.setOnClickListener(v -> selectTool(InteractionMode.SELECT_GOAL_NODE));
        toolEraser.setOnClickListener(v -> selectTool(InteractionMode.DELETE));
    }

    // --- PUBLIC GETTERS ---
    public String getCurrentSelectedAlgorithm() {
        return currentSelectedAlgorithm;
    }

    // --- GRAPH TYPE LOGIC ---
    private void selectDirected() {
        if (isDirectedSelected) return;
        isDirectedSelected = true;
        graph.setType(GraphType.DIRECTED);
        graphView.invalidate();

        viewThumb.animate().translationX(0f).setDuration(250).withEndAction(() -> {
            tvDirected.setTextColor(Color.parseColor("#000000"));
            tvDirected.setTypeface(null, Typeface.BOLD);
            tvUndirected.setTextColor(Color.parseColor("#FFFFFF"));
            tvUndirected.setTypeface(null, Typeface.NORMAL);
        }).start();
    }

    private void selectUndirected() {
        if (!isDirectedSelected) return;
        isDirectedSelected = false;
        graph.setType(GraphType.UNDIRECTED);
        graphView.invalidate();

        viewThumb.animate().translationX(viewThumb.getWidth()).setDuration(250).withEndAction(() -> {
            tvUndirected.setTextColor(Color.parseColor("#000000"));
            tvUndirected.setTypeface(null, Typeface.BOLD);
            tvDirected.setTextColor(Color.parseColor("#FFFFFF"));
            tvDirected.setTypeface(null, Typeface.NORMAL);
        }).start();
    }

    // --- DROPDOWN LOGIC ---
    private void toggleDropdown() {
        isDropdownExpanded = !isDropdownExpanded;
        if (isDropdownExpanded) {
            dropdownOptions.setVisibility(View.VISIBLE);
            dropdownArrow.animate().rotation(-90f).setDuration(200).start();
        } else {
            dropdownOptions.setVisibility(View.GONE);
            dropdownArrow.animate().rotation(0f).setDuration(200).start();
        }
    }
    public void highlightAlgorithmDropdown() {
        if (tvSelectedAlgorithm == null) return;

        // 1. The Shake: Move left and right, getting smaller each time (spring effect)
        android.animation.ObjectAnimator shake = android.animation.ObjectAnimator.ofFloat(
                tvSelectedAlgorithm, "translationX", 0f, 15f, -15f, 10f, -10f, 5f, -5f, 0f);

        // 2. The Color Flash: Flash the text to a bright Red, then smoothly back to default (Black/White)
        int defaultColor = tvSelectedAlgorithm.getCurrentTextColor();
        android.animation.ObjectAnimator colorFlash = android.animation.ObjectAnimator.ofArgb(
                tvSelectedAlgorithm, "textColor",
                defaultColor, android.graphics.Color.parseColor("#F44336"), defaultColor);

        // 3. Play them both together!
        android.animation.AnimatorSet warningAnimation = new android.animation.AnimatorSet();
        warningAnimation.playTogether(shake, colorFlash);
        warningAnimation.setDuration(500); // Half a second is perfect for a snappy warning
        warningAnimation.start();
    }
    private void handleOptionSelected(String algorithmName) {
        tvSelectedAlgorithm.setText(algorithmName);
        currentSelectedAlgorithm = algorithmName;
        if (isDropdownExpanded) toggleDropdown();
    }

    // --- SIDEBAR LOGIC ---
    private void selectTool(InteractionMode mode) {
        // Dim ALL tools to 50%
        toolNode.setAlpha(0.5f);
        toolEdge.setAlpha(0.5f);
        toolEraser.setAlpha(0.5f);
        toolStartNode.setAlpha(0.5f);
        toolGoalNode.setAlpha(0.5f);

        // Highlight only the selected one
        switch (mode) {
            case ADD_NODE: toolNode.setAlpha(1.0f); break;
            case ADD_EDGE: toolEdge.setAlpha(1.0f); break;
            case DELETE: toolEraser.setAlpha(1.0f); break;
            case SELECT_START_NODE: toolStartNode.setAlpha(1.0f); break;
            case SELECT_GOAL_NODE: toolGoalNode.setAlpha(1.0f); break;
        }
        graphView.setInteractionMode(mode);
    }

    // --- PREMIUM BUTTON LOGIC ---
    public void makeButtonFeelPremium(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }
}