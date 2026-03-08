package com.example.graphtreealgosimulator;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.graphtreealgosimulator.algorithms.AlgorithmFactory;
import com.example.graphtreealgosimulator.algorithms.AlgorithmListener;
import com.example.graphtreealgosimulator.algorithms.BFSAlgorithm;
import com.example.graphtreealgosimulator.algorithms.DFSAlgorithm;
import com.example.graphtreealgosimulator.algorithms.GraphAlgorithm;
import com.example.graphtreealgosimulator.model.Edge;
import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.GraphType;
import com.example.graphtreealgosimulator.model.Node;
import com.example.graphtreealgosimulator.view.GraphView;
import com.example.graphtreealgosimulator.view.InteractionMode;
import com.example.graphtreealgosimulator.view.OnEdgeSelectedListener;

import java.util.ArrayList;
import java.util.List;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.ViewGroup;
public class MainActivity extends AppCompatActivity implements OnEdgeSelectedListener{

    private GraphView graphView;
    private Graph graph;
    private Edge currentSelectedEdge;
    private PlaybackManager playbackManager;
    private int currentDepthLimit = 3;
    // UI Manager variables
    private UIManager uiManager; // --- NEW! ---

// terminal variables
    private TerminalManager terminalManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // --- EDGE-TO-EDGE SAFE AREA FIX ---
        View segmentedControl = findViewById(R.id.segmented_control);
        View bottomTerminal = findViewById(R.id.bottom_terminal);

        // Listen for the exact size of the phone's system bars (camera hole, bottom swipe bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 1. Push the top switch down past the camera hole!
            ViewGroup.MarginLayoutParams topParams = (ViewGroup.MarginLayoutParams) segmentedControl.getLayoutParams();
            topParams.topMargin = insets.top + 20; // insets.top is the exact height of the camera hole!
            segmentedControl.setLayoutParams(topParams);

            // 2. Give the bottom terminal some extra padding so it doesn't overlap the phone's swipe gesture bar!
            bottomTerminal.setPadding(
                    bottomTerminal.getPaddingLeft(),
                    bottomTerminal.getPaddingTop(),
                    bottomTerminal.getPaddingRight(),
                    insets.bottom + 16
            );

            return windowInsets;
        });
        graphView = findViewById(R.id.graphView);
        graph = new Graph(GraphType.DIRECTED);
        graphView.setGraph(graph);
        setupDefaultLogoGraph();
        graphView.setOnEdgeSelectedListener(this);

        // --- INITIALIZE MANAGERS ---
        uiManager = new UIManager(this, graphView, graph);

        terminalManager = new TerminalManager(
                this,
                findViewById(R.id.terminal_animation_area),
                findViewById(R.id.layout_queue_view),
                findViewById(R.id.layout_stack_view),
                findViewById(R.id.queue_container),
                findViewById(R.id.stack_container),
                findViewById(R.id.tv_terminal_status),
                findViewById(R.id.tv_algo_action)
        );
        playbackManager = new PlaybackManager(this, graphView, terminalManager);
        ImageButton btnPlayAlgorithm = findViewById(R.id.btn_play_algorithm);
        btnPlayAlgorithm.setOnClickListener(v -> {
            if (!playbackManager.isRunning()) {
                runAlgorithm(); // Generates the steps and hands them to the engine!
            } else {
                playbackManager.togglePausePlay(); // Tells the engine to pause/resume
            }
        });
        uiManager.makeButtonFeelPremium(btnPlayAlgorithm);

        View btnUndo = findViewById(R.id.btn_clear_grid);
        btnUndo.setOnClickListener(v -> clearGrid());
        uiManager.makeButtonFeelPremium(btnUndo);

        findViewById(R.id.btn_terminal_expand).setOnClickListener(v -> terminalManager.toggleTerminal());
    }


    private void setupDefaultLogoGraph() {
        // Node 1 (Top Center)
        com.example.graphtreealgosimulator.model.Node n1 = new com.example.graphtreealgosimulator.model.Node(1, 500f, 400f);
        // Node 2 (Bottom Left)
        com.example.graphtreealgosimulator.model.Node n2 = new com.example.graphtreealgosimulator.model.Node(2, 300f, 700f);
        // Node 3 (Bottom Right)
        com.example.graphtreealgosimulator.model.Node n3 = new com.example.graphtreealgosimulator.model.Node(3, 700f, 700f);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);

        graph.addEdge(n1, n2, 1);
        graph.addEdge(n1, n3, 1);

        // Tell the GraphView that the next node drawn should be ID 4!
        graphView.setNodeCounter(4);
        graphView.invalidate();
    }

    @Override
    public void onEdgeSelected(Edge edge) {
        currentSelectedEdge = edge;
        // Call the popup dialog when an edge is tapped
        showWeightInputDialog(edge);
    }

    private void showWeightInputDialog(Edge edge) {
        // Flash a quick message on the screen to prove the touch was detected!
        Toast.makeText(this, "Edge selected!", Toast.LENGTH_SHORT).show();

        // Using MainActivity.this ensures the dialog gets the correct app theme
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Set Edge Weight");

        // Create an EditText directly in Java
        final EditText input = new EditText(MainActivity.this);

        // Force the keyboard to only show numbers (and allow negative numbers)
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        // Pre-fill it with the current weight and move the cursor to the end
        input.setText(String.valueOf(edge.getWeight()));
        input.setSelection(input.getText().length());

        // Add some padding so it doesn't touch the edges of the box
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        // Set up the "OK" button
        builder.setPositiveButton("OK", (dialog, which) -> {
            String weightStr = input.getText().toString();
            if (!weightStr.isEmpty()) {
                try {
                    int weight = Integer.parseInt(weightStr);
                    edge.setWeight(weight); // Update your model
                    graphView.invalidate(); // Redraw the GraphView
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up the "Cancel" button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog!
        builder.show();
    }
    private void showDepthLimitDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Set Depth Limit");
        builder.setMessage("How deep should the algorithm search before backtracking?");

        // Create an input field and force it to only accept numbers
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g., 2, 3, 5");

        // Add a little padding so it looks nice
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 0);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        // When they click RUN
        builder.setPositiveButton("Run", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                currentDepthLimit = Integer.parseInt(val);
            } else {
                currentDepthLimit = 3; // Fallback if they leave it blank
            }
            // Now that we have the number, actually execute the math!
            startAlgorithmExecution();
        });

        // If they click Cancel, just close the box and reset the play button
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            android.widget.ImageButton btnPlayAlgorithm = findViewById(R.id.btn_play_algorithm);
            btnPlayAlgorithm.setBackground(getDrawable(R.drawable.play_button));
        });

        builder.show();
    }

    private void runAlgorithm() {
        String selectedAlgo = uiManager.getCurrentSelectedAlgorithm(); // Grab it from the manager

        if (graph.getNodes().isEmpty() || selectedAlgo.isEmpty() || selectedAlgo.equals("Algorithm")) {
            uiManager.highlightAlgorithmDropdown();
            //tell the terminal to prompt them too!
            terminalManager.setActionText("Please select an algorithm first!");
            return;
        }

        // If it's DLS, intercept the play button and show the popup!
        if (selectedAlgo.equals("DLS")) {
            showDepthLimitDialog();
        } else {
            // For all other algorithms, just run them normally
            startAlgorithmExecution();
        }
    }
    private void startAlgorithmExecution() {
        String selectedAlgo = uiManager.getCurrentSelectedAlgorithm(); // Grab it from the manager


        if (graph.getNodes().isEmpty() || selectedAlgo.isEmpty() || selectedAlgo.equals("Algorithm")) {
            uiManager.highlightAlgorithmDropdown();
            //tell the terminal to prompt them too!
            terminalManager.setActionText("Please select an algorithm first!");
            return;
        }

        // Reset the graph colors and terminal before starting
        graphView.resetTraversal();
        terminalManager.clear();

        // NEW: Reset the output text at the start of the run!
        terminalManager.setStatusText("Output: ");

        Node startNode = graphView.getStartNode();
        Node goalNode = graphView.getGoalNode();
        // Fallback: If they forgot to select a start node, just pick the first one!
        if (startNode == null) {
            startNode = graph.getNodes().get(0);
            // Optional: Visually snap it to the start node so they see what happened
            graphView.setInteractionMode(InteractionMode.SELECT_START_NODE);
        }
        List<Runnable> animationSteps = new ArrayList<>();
        // --- NEW: A list to track the nodes instantly before animation starts ---
        List<Node> expectedVisitedNodes = new ArrayList<>();

        // 1. Create the Listener that records the visual steps
        AlgorithmListener listener = new AlgorithmListener() {
            @Override
            public void onUpdateTerminal(String actionText, String nodeVal, boolean isPush, String structureType) {
                // FIX: We must use the new terminalManager to update the steps!
                animationSteps.add(() -> terminalManager.updateTerminalStep(actionText, nodeVal, isPush, structureType));
            }

            @Override
            public void onNodeProcessing(Node node) {
                animationSteps.add(() -> graphView.setCurrentNode(node));
            }

            @Override
            public void onNodeVisited(Node node) {
                // --- NEW: Add the node to our expected list instantly ---
                expectedVisitedNodes.add(node);

                animationSteps.add(() -> {
                    // Turn the node green on the canvas
                    graphView.addVisitedNode(node);
                    // add node to the output box
                    graphView.appendToResultBox(node.getId());
                    // Let the terminal manager handle the string math!
                    terminalManager.appendToOutput(String.valueOf(node.getId()));
                });
            }

            @Override
            public void onFinalResultComputed(String result) {
                animationSteps.add(() -> {
                    graphView.setFinalAlgorithmOutput(result);
                    terminalManager.setActionText("Shortest Paths Computed!");
                });
            }
        };

        // 2. Pass the listener into our new separate algorithm files
        // 2. Use the Factory to grab the right math engine, and run it!
        GraphAlgorithm algorithm = AlgorithmFactory.getAlgorithm(selectedAlgo);
        if (algorithm instanceof com.example.graphtreealgosimulator.algorithms.DLSAlgorithm) {
            ((com.example.graphtreealgosimulator.algorithms.DLSAlgorithm) algorithm).setDepthLimit(currentDepthLimit);
        }

        algorithm.run(graph, startNode, goalNode, listener);

        // --- Lock in the bounding box in GraphView BEFORE playing the animation ---
        if (selectedAlgo.equals("DLS")) {
            graphView.setAlgorithmBounds(graph.getNodes());
        } else {
            graphView.setAlgorithmBounds(expectedVisitedNodes);
        }

        // Hand the recorded steps over to the DVD Player!
        playbackManager.startPlayback(animationSteps);
    }
    private void clearGrid() {
        // Build a popup dialog to ask for confirmation
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear Blueprint")
                .setMessage("Are you sure you want to clear the entire grid? This cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    // --- THE USER CLICKED YES ---

                    // 1. Wipe the blueprint canvas and reset the node counter to 1
                    graphView.clearGraph();

                    // 2. Clear out the terminal UI
                    terminalManager.clear();
                    // --- KILL THE ENGINE ---
                    playbackManager.stopEngine();
                    // 3. Give the user satisfying feedback!
                    Toast.makeText(MainActivity.this, "Grid Cleared!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // --- THE USER CLICKED CANCEL ---
                    // Just close the dialog and do absolutely nothing!
                    dialog.dismiss();
                })
                .show(); // Don't forget to actually show the dialog!
    }
}