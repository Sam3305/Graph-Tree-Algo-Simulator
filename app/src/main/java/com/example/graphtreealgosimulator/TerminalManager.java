package com.example.graphtreealgosimulator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TerminalManager {
    private DualQueueManager dualQueueManager;
    private final Context context;
    private final LinearLayout terminalAnimationArea;
    private final LinearLayout layoutQueueView;
    private final LinearLayout layoutStackView;
    private final LinearLayout queueContainer;
    private final LinearLayout stackContainer;
    private final TextView tvTerminalStatus;
    private final TextView tvAlgoAction;
    private android.widget.LinearLayout fQueueRow;
    private android.widget.LinearLayout bQueueRow;
    private boolean isTerminalExpanded = false;
    private String traversalOutput = "";
    // Constructor: We pass in all the UI elements it needs to manage
    public TerminalManager(Context context, LinearLayout terminalAnimationArea,
                           LinearLayout layoutQueueView, LinearLayout layoutStackView,
                           LinearLayout queueContainer, LinearLayout stackContainer,
                           TextView tvTerminalStatus, TextView tvAlgoAction) {
        this.context = context;
        this.terminalAnimationArea = terminalAnimationArea;
        this.layoutQueueView = layoutQueueView;
        this.layoutStackView = layoutStackView;
        this.queueContainer = queueContainer;
        this.stackContainer = stackContainer;
        this.tvTerminalStatus = tvTerminalStatus;
        this.tvAlgoAction = tvAlgoAction;
        this.dualQueueManager = new DualQueueManager(context, queueContainer);
    }

    public void toggleTerminal() {
        isTerminalExpanded = !isTerminalExpanded;
        terminalAnimationArea.setVisibility(isTerminalExpanded ? View.VISIBLE : View.GONE);
    }

    public void setStatusText(String text) {
        tvTerminalStatus.setText(text);
    }

    public void setActionText(String text) {
        tvAlgoAction.setText(text);
    }

    public void clear() {
        queueContainer.removeAllViews();
        stackContainer.removeAllViews();
        traversalOutput = ""; // <-- Wipe it!
        queueContainer.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        setStatusText("Terminal");
        setActionText("Waiting for algorithm...");
        if (dualQueueManager != null) {
            dualQueueManager.clear();
        }
    }
    public void appendToOutput(String nodeVal) {
        if (traversalOutput.isEmpty()) {
            traversalOutput = nodeVal;
        } else {
            traversalOutput += " -> " + nodeVal;
        }
        setStatusText("Output: " + traversalOutput);
    }

    public void updateTerminalStep(String actionText, String nodeVal, boolean isPush, String structureType) {
        if (!isTerminalExpanded) toggleTerminal();
        tvAlgoAction.setText(actionText);

        if (structureType.equals("QUEUE")) {
            layoutQueueView.setVisibility(View.VISIBLE);
            layoutStackView.setVisibility(View.GONE);
            handleQueueAnimation(nodeVal, isPush);
        } else if (structureType.equals("STACK")) {
            layoutStackView.setVisibility(View.VISIBLE);
            layoutQueueView.setVisibility(View.GONE);
            handleStackAnimation(nodeVal, isPush);
        }
        // --- NEW: Route Priority Queue commands here! ---
        else if (structureType.equals("PRIORITY_QUEUE")) {
            layoutQueueView.setVisibility(View.VISIBLE);
            layoutStackView.setVisibility(View.GONE);
            handlePriorityQueueAnimation(nodeVal, isPush);
        }
        else if (structureType.equals("QUEUE_F") || structureType.equals("QUEUE_B")) {
            // WE MUST MAKE SURE THE QUEUE CONTAINER IS VISIBLE!
            layoutQueueView.setVisibility(View.VISIBLE);
            layoutStackView.setVisibility(View.GONE);

            if (structureType.equals("QUEUE_F")) {
                dualQueueManager.handleQueue(nodeVal, isPush, true);
            } else {
                dualQueueManager.handleQueue(nodeVal, isPush, false);
            }
        }
    }

    private void handleQueueAnimation(String nodeVal, boolean isPush) {
        if (isPush) {
            TextView block = createDataBlock(nodeVal, 70, 70);
            queueContainer.addView(block);
        } else {
            if (queueContainer.getChildCount() > 0) {
                queueContainer.removeViewAt(0);
            }
        }
    }

    private void handleStackAnimation(String nodeVal, boolean isPush) {
        if (isPush) {
            TextView block = createDataBlock(nodeVal + "  ← Top", LinearLayout.LayoutParams.MATCH_PARENT, 80);
            if (stackContainer.getChildCount() > 0) {
                TextView oldTop = (TextView) stackContainer.getChildAt(0);
                oldTop.setText(oldTop.getText().toString().replace("  ← Top", ""));
            }
            stackContainer.addView(block, 0);
        } else {
            if (stackContainer.getChildCount() > 0) {
                stackContainer.removeViewAt(0);
                if (stackContainer.getChildCount() > 0) {
                    TextView newTop = (TextView) stackContainer.getChildAt(0);
                    newTop.setText(newTop.getText().toString() + "  ← Top");
                }
            }
        }
    }
    private void handlePriorityQueueAnimation(String nodeVal, boolean isPush) {
        // 1. Decode the secret message! Now supports an optional 3rd parameter for custom display text.
        String[] parts = nodeVal.split("\\|");
        String id = parts[0];
        int distance = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

        // NEW: If the algorithm sent a custom display string (like "10 + 5"), use it! Otherwise, just use the distance.
        String displayDist = parts.length > 2 ? parts[2] : String.valueOf(distance);

        if (isPush) {
            // Remove the old block for this node if it exists (No duplicates!)
            for (int i = 0; i < queueContainer.getChildCount(); i++) {
                View v = queueContainer.getChildAt(i);
                if (id.equals(v.getContentDescription())) {
                    queueContainer.removeViewAt(i);
                    break;
                }
            }


            TextView block = createDataBlock(id + "\n(" + displayDist + ")", 120, 90);
            block.setContentDescription(id);
            block.setTag(distance);          // We still use the REAL distance for mathematically sorting the line!
            block.setTextSize(13f);          // Shrink font slightly to be safe

            // 4. THE MAGIC: Find the exact sorted spot to insert this new block! (Min-Heap sorting)
            int insertIndex = queueContainer.getChildCount(); // Default to the back of the line
            for (int i = 0; i < queueContainer.getChildCount(); i++) {
                View v = queueContainer.getChildAt(i);
                if (v.getTag() != null) {
                    int vDist = (int) v.getTag();
                    // If the new block has a SHORTER distance than the block we are looking at...
                    if (distance < vDist) {
                        insertIndex = i; // CUT IN LINE HERE!
                        break;
                    }
                }
            }

            // Actually insert the block into the physical screen layout at the sorted index!
            queueContainer.addView(block, insertIndex);

        } else {
            // 5. Dequeue: Find the EXACT node by its ID and remove it.
            // (Because it is sorted, this should almost always be the one at the very front!)
            for (int i = 0; i < queueContainer.getChildCount(); i++) {
                View v = queueContainer.getChildAt(i);
                if (id.equals(v.getContentDescription())) {
                    queueContainer.removeViewAt(i);
                    return;
                }
            }
        }
    }

    private TextView createDataBlock(String text, int width, int height) {
        TextView block = new TextView(context);
        block.setText(text);
        block.setTextSize(18f);
        block.setTextColor(Color.BLACK);
        block.setTypeface(null, Typeface.BOLD);
        block.setGravity(android.view.Gravity.CENTER);
        block.setPadding(16, 0, 16, 0);

        // Standardizing pixels using display metrics so it looks good on all phones
        float density = context.getResources().getDisplayMetrics().density;
        int pxWidth = width == LinearLayout.LayoutParams.MATCH_PARENT ? width : (int) (width * density);
        int pxHeight = (int) (height * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pxWidth, pxHeight);
        block.setLayoutParams(params);
        return block;
    }
}