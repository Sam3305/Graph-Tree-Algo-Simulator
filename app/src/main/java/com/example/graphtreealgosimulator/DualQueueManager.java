package com.example.graphtreealgosimulator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DualQueueManager {

    private final LinearLayout parentContainer;
    private final Context context;

    private LinearLayout fQueueRow;
    private LinearLayout bQueueRow;
    private boolean isInitialized = false;

    public DualQueueManager(Context context, LinearLayout parentContainer) {
        this.context = context;
        this.parentContainer = parentContainer;
    }

    public void handleQueue(String nodeVal, boolean isPush, boolean isForward) {
        if (!isInitialized || parentContainer.indexOfChild(fQueueRow) == -1) {
            setupDualLayout();
        }

        LinearLayout targetRow = isForward ? fQueueRow : bQueueRow;

        if (isPush) {
            TextView block = createQueueBlock(nodeVal, isForward);
            targetRow.addView(block);
        } else {
            // Dequeue: Find the target block and safely remove it
            for (int i = 0; i < targetRow.getChildCount(); i++) {
                View v = targetRow.getChildAt(i);
                if (nodeVal.equals(v.getContentDescription())) {
                    targetRow.removeViewAt(i);
                    break;
                }
            }
        }
    }

    private void setupDualLayout() {
        parentContainer.removeAllViews();
        parentContainer.setOrientation(LinearLayout.VERTICAL);

        float density = context.getResources().getDisplayMetrics().density;

        // --- THE FIX: Scale down to 45dp so both rows fit inside your XML container! ---
        int rowHeight = (int) (30 * density);
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        // --- UPPER QUEUE (FORWARD) ---
        fQueueRow = new LinearLayout(context);
        fQueueRow.setOrientation(LinearLayout.HORIZONTAL);
        fQueueRow.setGravity(Gravity.CENTER_VERTICAL);
        fQueueRow.setMinimumWidth(screenWidth); // Stops the line from collapsing
        fQueueRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, rowHeight));

        // --- STATIC DIVIDER LINE ---
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#000000")); // Solid Black
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (2 * density))); // 2dp thick

        // --- LOWER QUEUE (BACKWARD) ---
        bQueueRow = new LinearLayout(context);
        bQueueRow.setOrientation(LinearLayout.HORIZONTAL);
        bQueueRow.setGravity(Gravity.CENTER_VERTICAL);
        bQueueRow.setMinimumWidth(screenWidth);
        bQueueRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, rowHeight));

        // Add them to the vertical container
        parentContainer.addView(fQueueRow);
        parentContainer.addView(divider);
        parentContainer.addView(bQueueRow);

        isInitialized = true;
    }

    private TextView createQueueBlock(String text, boolean isForward) {
        TextView block = new TextView(context);
        block.setText(text);
        block.setContentDescription(text);
        block.setTextSize(16f); // Slightly smaller text to fit the new block size perfectly
        block.setTypeface(null, Typeface.BOLD);
        block.setGravity(Gravity.CENTER);

        // Just the colored text as requested!
        if (isForward) {
            block.setTextColor(Color.parseColor("#2196F3")); // Bright Blue
        } else {
            block.setTextColor(Color.parseColor("#F44336")); // Bright Red
        }

        float density = context.getResources().getDisplayMetrics().density;

        int size = (int) (20 * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins((int)(4 * density), 0, (int)(4 * density), 0);
        block.setLayoutParams(params);

        // Keep the background completely transparent
        block.setBackgroundColor(Color.TRANSPARENT);

        return block;
    }

    public void clear() {
        isInitialized = false;
        fQueueRow = null;
        bQueueRow = null;
    }
}