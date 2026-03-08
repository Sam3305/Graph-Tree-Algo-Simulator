package com.example.graphtreealgosimulator.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.graphtreealgosimulator.model.Edge;
import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.GraphType;
import com.example.graphtreealgosimulator.model.Node;

import java.util.HashSet;
import java.util.Set;

public class GraphView extends View {

    private Graph graph;

    private Node startNode = null;
    private Node goalNode = null;

    private static final float NODE_RADIUS = 80f;
    private static final float SMALL_GRID = 100f;
    private static final float MAJOR_GRID = 200f;
    private InteractionMode mode = InteractionMode.ADD_NODE;
    private Paint nodePaint;
    private Paint edgePaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint majorGridPaint;
    private Paint weightBgPaint;

    private int nodeCounter = 1; // this is node namer
    private Node draggingNode = null;
    private Edge selectedEdge = null;
    private OnEdgeSelectedListener edgeSelectedListener;

    //dragging node variables
    private float touchOffsetX = 0f; // Keeps the drag smooth
    private float touchOffsetY = 0f;
    private Node firstSelectedNodeForEdge = null;

    // Traversal tracking
    private Node currentNode = null; // The node currently being processed (Yellow)
    private Set<Node> visitedNodes = new HashSet<>(); // Nodes completely done (Green)

    // Animation Variables
    private float hoverOffset = 0f;
    private boolean isIdleAnimating = true;
    private ValueAnimator hoverAnimator;
    private float boxStartX = 0f;
    private float boxStartY = 0f;
    // Camera Variables
    private float scaleFactor = 1.0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float lastTouchX;
    private float lastTouchY;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean isPanning = false;

    // --- Output Box Variables ---
    private java.util.List<Integer> traversalResult = new java.util.ArrayList<>();
    private Paint resultBoxBorderPaint;
    private Paint resultBoxBgPaint;
    private Paint resultTextPaint;
    private String finalAlgorithmOutput = ""; // Tracks the multiline Dijkstra report
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setInteractionMode(InteractionMode mode) {
        this.mode = mode;
    }
    public Node getStartNode() { return startNode; }
    public Node getGoalNode() { return goalNode; }

    private void init() {

        // Node paint (light blueprint white)
        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodePaint.setColor(Color.parseColor("#FFFFFF"));
        nodePaint.setStyle(Paint.Style.FILL);

        // Edge paint (white smooth lines)
        edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgePaint.setColor(Color.parseColor("#FFFFFF"));
        edgePaint.setStrokeWidth(4f);
        edgePaint.setStrokeCap(Paint.Cap.ROUND);

        // Text paint (white)
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#000000"));
        textPaint.setTextSize(36f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Small grid paint (subtle blue)
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#B1B1B1"));
        gridPaint.setStrokeWidth(1.5f);

        // Major grid paint (stronger lines)
        majorGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        majorGridPaint.setColor(Color.parseColor("#D9D9D9"));
        majorGridPaint.setStrokeWidth(3f);
        // Weight Background Paint (Solid White Pill)
        weightBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        weightBgPaint.setColor(Color.parseColor("#FFFFFF"));
        weightBgPaint.setStyle(Paint.Style.FILL);

        // Dashed Amber Border for the output box
        resultBoxBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultBoxBorderPaint.setColor(Color.parseColor("#FFCA28"));
        resultBoxBorderPaint.setStyle(Paint.Style.STROKE);
        resultBoxBorderPaint.setStrokeWidth(6f);
        resultBoxBorderPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{20f, 10f}, 0f));

        // Semi-transparent Amber Background
        resultBoxBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultBoxBgPaint.setColor(Color.parseColor("#1AFFCA28")); // 10% Opacity Amber
        resultBoxBgPaint.setStyle(Paint.Style.FILL);

        // Yellow Text for the Box Label
        resultTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultTextPaint.setColor(Color.parseColor("#FFCA28"));
        resultTextPaint.setTextSize(40f);
        resultTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));


        //call hovering animation
        startIdleAnimation();

        // Initialize the Pinch-to-Zoom detector
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        invalidate();
    }
    public void setNodeCounter(int count) {
        this.nodeCounter = count;
    }
    private void startIdleAnimation() {
        hoverAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        hoverAnimator.setDuration(3000); // 3 seconds for a full up/down breath
        hoverAnimator.setRepeatCount(ValueAnimator.INFINITE);
        hoverAnimator.setInterpolator(new LinearInterpolator());

        hoverAnimator.addUpdateListener(animation -> {
            if (isIdleAnimating) {
                float value = (float) animation.getAnimatedValue();
                // Sine wave math: Bob smoothly up and down by 15 pixels
                hoverOffset = (float) Math.sin(value) * 15f;
                invalidate(); // Force the canvas to redraw constantly!
            }
        });
        hoverAnimator.start();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. FLOOD FILL THE BACKGROUND!
        // Doing this before the camera moves guarantees it covers the infinite void.
        canvas.drawColor(Color.parseColor("#355C8A")); // Your solid blueprint blue!

        // 2. SAVE the default canvas state
        canvas.save();

        // 3. MOVE and ZOOM the camera
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);

        // Draw the infinite grid on top of the blue background
        drawInfiniteGrid(canvas);

        if (graph == null) {
            canvas.restore();
            return;
        }

        drawEdges(canvas);
        drawNodes(canvas);
        drawResultBox(canvas);

        // 4. RESTORE the canvas so it's ready for the next frame
        canvas.restore();
    }


    private float snapToGrid(float value) {
        return Math.round(value / SMALL_GRID) * SMALL_GRID;
    }

    private void drawEdges(Canvas canvas) {
        for (Edge edge : graph.getEdges()) {
            float startX = edge.getFrom().getX();
            float startY = edge.getFrom().getY()+ hoverOffset;
            float endX = edge.getTo().getX();
            float endY = edge.getTo().getY()+ hoverOffset;

            if (edge.equals(selectedEdge)) {
                edgePaint.setStrokeWidth(8f);
            } else {
                edgePaint.setStrokeWidth(4f);
            }

            // CHECK MODE: Draw arrow if directed, normal line if undirected!
            if (graph.getType() == GraphType.DIRECTED) {
                drawArrow(canvas, startX, startY, endX, endY, edgePaint);
            } else {
                canvas.drawLine(startX, startY, endX, endY, edgePaint);
            }

            // Draw Weight Text in the middle
            float midX = (startX + endX) / 2;
            float midY = (startY + endY) / 2;

            String weightText = String.valueOf(edge.getWeight());
            float textWidth = textPaint.measureText(weightText);

            // Get the actual height metrics of the font
            Paint.FontMetrics fm = textPaint.getFontMetrics();

            // Where the text will physically start drawing
            float textX = midX - textWidth / 2;
            float textY = midY - 10;

            // How much padding (margins) we want inside the background pill
            float paddingX = 20f;
            float paddingY = 10f;

            // Calculate the 4 corners of the background pill based on the text size!
            android.graphics.RectF bgRect = new android.graphics.RectF(
                    textX - paddingX,
                    textY + fm.ascent - paddingY, // ascent is the top of the text
                    textX + textWidth + paddingX,
                    textY + fm.descent + paddingY // descent is the bottom of the text
            );

            // 1. Draw the rounded background pill first
            canvas.drawRoundRect(bgRect, 15f, 15f, weightBgPaint);

            // 2. Draw the text on top of it
            canvas.drawText(weightText, textX, textY, textPaint);
        }
    }

    public void setOnEdgeSelectedListener(OnEdgeSelectedListener listener) {
        this.edgeSelectedListener = listener;
    }

    private void drawNodes(Canvas canvas) {
        for (Node node : graph.getNodes()) {

            // 1. Pick the color based on the algorithm state
            if (node.equals(currentNode)) {
                nodePaint.setColor(Color.parseColor("#FFEB3B")); // Bright Yellow (Processing)
            } else if (visitedNodes.contains(node)) {
                nodePaint.setColor(Color.parseColor("#4CAF50")); // Green (Visited)
            } else if (node.equals(startNode)) {
                nodePaint.setColor(Color.parseColor("#2196F3")); // Blue (Start Node)
            } else if (node.equals(goalNode)) {
                nodePaint.setColor(Color.parseColor("#F44336")); // Red (Goal Node)
            } else {
                nodePaint.setColor(Color.parseColor("#FFFFFF")); // Default White
            }

            // 2. Draw circle
            canvas.drawCircle(node.getX(), node.getY()+ hoverOffset, NODE_RADIUS, nodePaint);

            // 3. Draw node ID centered
            String text = String.valueOf(node.getId());
            float textWidth = textPaint.measureText(text);
            float textHeight = textPaint.getTextSize();
            canvas.drawText(text, node.getX() - textWidth / 2, node.getY()+ hoverOffset + textHeight / 3, textPaint);
        }
    }

    private Node getNodeAt(float x, float y) {

        for (Node node : graph.getNodes()) {

            float dx = node.getX() - x;
            float dy = node.getY() - y;

            if (Math.sqrt(dx * dx + dy * dy) <= NODE_RADIUS) {
                return node;
            }
        }

        return null;
    }

    private float distanceToSegment(float px, float py,
                                    float x1, float y1,
                                    float x2, float y2) {

        float dx = x2 - x1;
        float dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            dx = px - x1;
            dy = py - y1;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        float t = ((px - x1) * dx + (py - y1) * dy) /
                (dx * dx + dy * dy);

        t = Math.max(0, Math.min(1, t));

        float nearestX = x1 + t * dx;
        float nearestY = y1 + t * dy;

        float distX = px - nearestX;
        float distY = py - nearestY;

        return (float) Math.sqrt(distX * distX + distY * distY);
    }

    private Edge getEdgeAt(float x, float y) {

        final float TOUCH_THRESHOLD = 100f;

        for (Edge edge : graph.getEdges()) {

            float x1 = edge.getFrom().getX();
            float y1 = edge.getFrom().getY();
            float x2 = edge.getTo().getX();
            float y2 = edge.getTo().getY();

            float distance = distanceToSegment(x, y, x1, y1, x2, y2);

            if (distance <= TOUCH_THRESHOLD) {
                return edge;
            }
        }

        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (graph == null) return true;

        // 1. Let the Pinch-to-Zoom detector inspect the touch first!
        scaleGestureDetector.onTouchEvent(event);

        // 2. The Physical Screen Coordinates (Where your physical finger is on the glass)
        float screenX = event.getX();
        float screenY = event.getY();

        // 3. The World Coordinates (Where your finger is pointing on the blueprint grid)
        float worldX = (screenX - translateX) / scaleFactor;
        float worldY = (screenY - translateY) / scaleFactor;

        // 4. Act as a Switchboard to route the event to the correct helper method
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(worldX, worldY, screenX, screenY);
                break;

            case MotionEvent.ACTION_MOVE:
                handleActionMove(worldX, worldY, screenX, screenY);
                break;

            case MotionEvent.ACTION_UP:
                handleActionUp(worldX, worldY);
                break;
        }

        return true;
    }
    // ==========================================
    // TOUCH LIFECYCLE HELPERS
    // ==========================================

    private void handleActionDown(float worldX, float worldY, float screenX, float screenY) {
        // SNAP THE ANIMATION OFF IF THEY TOUCH THE SCREEN
        if (isIdleAnimating) {
            isIdleAnimating = false;
            hoverOffset = 0f;
            if (hoverAnimator != null) hoverAnimator.cancel();
            invalidate();
        }

        // Track the start of the touch for camera panning
        lastTouchX = screenX;
        lastTouchY = screenY;
        isPanning = false;

        // Route to the specific tool logic!
        if (mode == InteractionMode.DELETE) {
            handleEraserTap(worldX, worldY);
        } else if (mode == InteractionMode.ADD_EDGE) {
            handleEdgeToolTap(worldX, worldY);
        } else if (mode == InteractionMode.ADD_NODE) {
            handleNodeToolDown(worldX, worldY);
        } else if (mode == InteractionMode.SELECT_START_NODE) {
            Node tappedNode = getNodeAt(worldX, worldY);
            if (tappedNode != null) {
                startNode = tappedNode;
                invalidate();
            }
        } else if (mode == InteractionMode.SELECT_GOAL_NODE) {
            Node tappedNode = getNodeAt(worldX, worldY);
            if (tappedNode != null) {
                goalNode = tappedNode;
                invalidate();
            }
        }
    }

    private void handleEraserTap(float worldX, float worldY) {
        Node node = getNodeAt(worldX, worldY);
        if (node != null) {
            if (node.equals(startNode)) startNode = null;
            if (node.equals(goalNode)) goalNode = null;
            graph.removeNode(node);
            recalculateNodeCounter();
            invalidate();
            return;
        }

        Edge tappedEdge = getEdgeAt(worldX, worldY);
        if (tappedEdge != null){
            graph.getEdges().remove(tappedEdge);
            invalidate();
        }
    }

    private void recalculateNodeCounter() {
        int maxId = 0;
        for (Node n : graph.getNodes()) {
            if (n.getId() > maxId) maxId = n.getId();
        }
        nodeCounter = maxId + 1;
    }

    private void handleEdgeToolTap(float worldX, float worldY) {
        Node tappedNode = getNodeAt(worldX, worldY);

        if (tappedNode != null) {
            if (firstSelectedNodeForEdge == null) {
                firstSelectedNodeForEdge = tappedNode;
            } else if (firstSelectedNodeForEdge != tappedNode) {
                graph.addEdge(firstSelectedNodeForEdge, tappedNode, 1);
                firstSelectedNodeForEdge = null;
            }
        } else {
            Edge tappedEdge = getEdgeAt(worldX, worldY);
            if (tappedEdge != null) {
                selectedEdge = tappedEdge;
                if (edgeSelectedListener != null) {
                    edgeSelectedListener.onEdgeSelected(tappedEdge);
                }
            } else {
                firstSelectedNodeForEdge = null; // clicked empty space, cancel edge drawing
            }
        }
        invalidate();
    }

    private void handleNodeToolDown(float worldX, float worldY) {
        draggingNode = getNodeAt(worldX, worldY);
        if (draggingNode != null) {
            touchOffsetX = worldX - draggingNode.getX();
            touchOffsetY = worldY - draggingNode.getY();
        }
    }private void handleActionMove(float worldX, float worldY, float screenX, float screenY) {
        // If the user is actively pinching with two fingers, ignore everything else
        if (scaleGestureDetector.isInProgress()) return;

        float dx = screenX - lastTouchX;
        float dy = screenY - lastTouchY;

        // If they moved their finger more than a tiny wobble (10 pixels), it's a pan!
        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
            isPanning = true;
        }

        if (draggingNode != null) {
            draggingNode.setX(worldX - touchOffsetX);
            draggingNode.setY(worldY - touchOffsetY);
            invalidate();
        } else if (isPanning) {
            translateX += dx;
            translateY += dy;
            invalidate();
        }

        lastTouchX = screenX;
        lastTouchY = screenY;
    }

    private void handleActionUp(float worldX, float worldY) {
        if (draggingNode != null) {
            // Let go of the dragged node and snap it to the grid
            float snappedX = snapToGrid(draggingNode.getX());
            float snappedY = snapToGrid(draggingNode.getY());
            draggingNode.setX(snappedX);
            draggingNode.setY(snappedY);

            draggingNode = null;
            invalidate();
        }
        // IF we were in Node Mode, AND we tapped empty space, AND we didn't drag...
        else if (mode == InteractionMode.ADD_NODE && !isPanning) {
            float snappedX = snapToGrid(worldX);
            float snappedY = snapToGrid(worldY);
            Node newNode = new Node(nodeCounter++, snappedX, snappedY);
            graph.addNode(newNode);
            performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            invalidate();
        }

        // Reset the panning flag when they lift their finger
        isPanning = false;
    }


    private void drawArrow(Canvas canvas, float startX, float startY, float endX, float endY, Paint paint) {
        float dx = endX - startX;
        float dy = endY - startY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Prevent division by zero if nodes are perfectly stacked
        if (distance == 0) return;

        // 1. Calculate the exact point where the line touches the destination circle
        // We subtract NODE_RADIUS so the arrow stops right at the edge of the node
        float ratio = (distance - NODE_RADIUS) / distance;
        float tipX = startX + dx * ratio;
        float tipY = startY + dy * ratio;

        // Draw the main line
        canvas.drawLine(startX, startY, tipX, tipY, paint);

        // 2. Calculate the angle of the line
        double angle = Math.atan2(dy, dx);

        // Arrowhead size and angle settings
        float arrowLength = 40f;
        double arrowAngle = Math.PI / 6; // 30 degrees

        // 3. Calculate the two back corners of the arrowhead triangle
        float x1 = (float) (tipX - arrowLength * Math.cos(angle - arrowAngle));
        float y1 = (float) (tipY - arrowLength * Math.sin(angle - arrowAngle));

        float x2 = (float) (tipX - arrowLength * Math.cos(angle + arrowAngle));
        float y2 = (float) (tipY - arrowLength * Math.sin(angle + arrowAngle));

        // 4. Draw the filled triangle
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(tipX, tipY);
        path.lineTo(x1, y1);
        path.lineTo(x2, y2);
        path.close();

        // Temporarily set paint to FILL so the arrowhead is solid, then revert it
        Paint.Style originalStyle = paint.getStyle();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
        paint.setStyle(originalStyle);
    }
    public void setCurrentNode(Node node) {
        this.currentNode = node;
        invalidate();
    }

    public void addVisitedNode(Node node) {
        this.visitedNodes.add(node);
        invalidate();
    }
    public void appendToResultBox(int nodeId) {
        traversalResult.add(nodeId);
        invalidate(); // Redraw the screen to show the new node in the box!
    }
    public void resetTraversal() {
        this.currentNode = null;
        this.visitedNodes.clear();
        this.traversalResult.clear(); // WIPE THE OUTPUT BOX!
        this.finalAlgorithmOutput = ""; // WIPE THE TEXT!
        invalidate();
    }

    // Wipes the canvas clean and resets the node counter
    public void clearGraph() {
        if (graph != null) {
            graph.clear();
        }
        startNode = null;
        goalNode = null;
        nodeCounter = 1;  // Reset node naming back to '1'
        resetTraversal(); // Clear any yellow/green algorithm colors
        invalidate();     // Force the canvas to redraw completely empty

    }

    public void setFinalAlgorithmOutput(String output) {
        this.finalAlgorithmOutput = output;
        invalidate(); // Redraw immediately!
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            // Restrict zoom: Don't let them zoom out too far (0.8x) or zoom in too close (1.5x)
            scaleFactor = Math.max(0.8f, Math.min(scaleFactor, 1.5f));
            invalidate(); // Force a redraw
            return true;
        }
    }

    private void drawInfiniteGrid(Canvas canvas) {
        // 1. Calculate the exact boundaries of what the camera is currently looking at
        float left = -translateX / scaleFactor;
        float top = -translateY / scaleFactor;
        float right = left + (getWidth() / scaleFactor);
        float bottom = top + (getHeight() / scaleFactor);

        // 2. Snap the starting coordinates to the nearest grid intersection
        float startX = (float) Math.floor(left / SMALL_GRID) * SMALL_GRID;
        float startY = (float) Math.floor(top / SMALL_GRID) * SMALL_GRID;

        // 3. Draw Vertical Lines (from left to right across the screen)
        for (float x = startX; x <= right; x += SMALL_GRID) {
            // If the line is a multiple of the major grid size, draw it thicker!
            if (Math.abs(x % MAJOR_GRID) < 1f) {
                canvas.drawLine(x, top, x, bottom, majorGridPaint);
            } else {
                canvas.drawLine(x, top, x, bottom, gridPaint);
            }
        }

        // 4. Draw Horizontal Lines (from top to bottom across the screen)
        for (float y = startY; y <= bottom; y += SMALL_GRID) {
            if (Math.abs(y % MAJOR_GRID) < 1f) {
                canvas.drawLine(left, y, right, y, majorGridPaint);
            } else {
                canvas.drawLine(left, y, right, y, gridPaint);
            }
        }
    }
    public void setAlgorithmBounds(java.util.List<Node> targetNodes) {
        if (targetNodes == null || targetNodes.isEmpty()) return;

        float minX = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        // Calculate the bounding box based on the ENTIRE final traversal
        for (Node n : targetNodes) {
            if (n.getX() < minX) minX = n.getX();
            if (n.getY() > maxY) maxY = n.getY();
        }

        // Lock in the final coordinates!
        this.boxStartX = minX;
        this.boxStartY = maxY + 400f;
    }
    private void drawResultBox(Canvas canvas) {
        if (traversalResult.isEmpty() || visitedNodes.isEmpty()) return;

        float startX = this.boxStartX;
        float startY = this.boxStartY;
        float spacing = 250f;
        float boxPadding = 120f;

        // --- NEW: Calculate extra space for multi-line text! ---
        float extraHeight = 0f;
        float maxTextWidth = 0f;
        String[] resultLines = null;

        if (!finalAlgorithmOutput.isEmpty()) {
            resultLines = finalAlgorithmOutput.split("\n");
            extraHeight = resultLines.length * 55f + 40f; // 55px per line of text

            // Find the longest sentence so we can stretch the box width!
            for (String line : resultLines) {
                float w = resultTextPaint.measureText(line);
                if (w > maxTextWidth) maxTextWidth = w;
            }
        }

        // Calculate the physical size of the perimeter box
        float boxLeft = startX - boxPadding;
        float boxTop = startY - boxPadding;

        // Ensure the box is wide enough for EITHER the node circles OR the text!
        float sequenceRight = startX + (traversalResult.size() - 1) * spacing + boxPadding;
        float textRight = boxLeft + maxTextWidth + 80f;
        float boxRight = Math.max(sequenceRight, textRight);

        float boxBottom = startY + boxPadding + extraHeight;

        // 3. Draw the box background and dashed border
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, resultBoxBgPaint);
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, resultBoxBorderPaint);

        // 4. Draw the label in the top-left corner
        canvas.drawText("ALGORITHM OUTPUT", boxLeft + 20f, boxTop + 45f, resultTextPaint);


        // ==========================================
        // 5. DRAW ALL EDGES FIRST (So they stay behind)
        // ==========================================
        for (int i = 1; i < traversalResult.size(); i++) {
            float nodeX = startX + (i * spacing);
            float nodeY = startY;
            float prevX = startX + ((i - 1) * spacing);

            // Reusing your arrow math
            drawArrow(canvas, prevX, nodeY, nodeX, nodeY, edgePaint);
        }

        // 6. DRAW ALL NODES SECOND (So they sit on top)
        for (int i = 0; i < traversalResult.size(); i++) {
            float nodeX = startX + (i * spacing);
            float nodeY = startY;

            // Draw the node circle (Green for Visited!)
            nodePaint.setColor(Color.parseColor("#4CAF50"));
            canvas.drawCircle(nodeX, nodeY, NODE_RADIUS, nodePaint);

            // Draw the node ID text
            String text = String.valueOf(traversalResult.get(i));
            float textWidth = textPaint.measureText(text);
            float textHeight = textPaint.getTextSize();
            canvas.drawText(text, nodeX - textWidth / 2, nodeY + textHeight / 3, textPaint);

            // --- NEW: Draw the multi-line text report below the nodes! ---
            if (resultLines != null) {
                float textY = startY + boxPadding + 60f; // Start below the node circles
                resultTextPaint.setTextSize(35f); // Slightly smaller font for the list
                resultTextPaint.setColor(Color.WHITE); // White text pops nicely!

                for (String line : resultLines) {
                    canvas.drawText(line, boxLeft + 40f, textY, resultTextPaint);
                    textY += 55f; // Move down for the next line
                }

                // Reset the paint size/color so the "ALGORITHM OUTPUT" header looks right next frame
                resultTextPaint.setTextSize(40f);
                resultTextPaint.setColor(Color.parseColor("#FFCA28"));
            }
        }
    }
    public void frameTraversalResult() {
        if (visitedNodes.isEmpty() || traversalResult.isEmpty()) return;

        // 1. Find the boundaries of the visited nodes
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for (Node n : visitedNodes) {
            if (n.getX() < minX) minX = n.getX();
            if (n.getX() > maxX) maxX = n.getX();
            if (n.getY() < minY) minY = n.getY();
            if (n.getY() > maxY) maxY = n.getY();
        }

        // 2. Add the boundaries of the result box so it doesn't get cut off!
        float startX = minX;
        float startY = maxY + 400f;
        float spacing = 250f;
        float boxPadding = 120f;

        float extraHeight = 0f;
        float maxTextWidth = 0f;
        if (!finalAlgorithmOutput.isEmpty()) {
            String[] resultLines = finalAlgorithmOutput.split("\n");
            extraHeight = resultLines.length * 55f + 40f;
            for (String line : resultLines) {
                float w = resultTextPaint.measureText(line);
                if (w > maxTextWidth) maxTextWidth = w;
            }
        }

        float boxLeft = startX - boxPadding;
        float sequenceRight = startX + (traversalResult.size() - 1) * spacing + boxPadding;
        float textRight = boxLeft + maxTextWidth + 80f;
        float boxRight = Math.max(sequenceRight, textRight);
        float boxBottom = startY + boxPadding + extraHeight;

        // Expand our camera target window to include the box
        if (boxLeft < minX) minX = boxLeft;
        if (boxRight > maxX) maxX = boxRight;
        if (boxBottom > maxY) maxY = boxBottom;

        // 3. Calculate target zoom scale
        float padding = 250f; // Give it 250px of breathing room on the screen edges
        float contentWidth = maxX - minX + padding * 2;
        float contentHeight = maxY - minY + padding * 2;

        float targetScaleX = getWidth() / contentWidth;
        float targetScaleY = getHeight() / contentHeight;
        // FIX: Use a temporary variable for the raw math
        float rawScale = Math.min(targetScaleX, targetScaleY);

        // FIX: Declare the final clamped variable as 'final' so the lambda is happy!
        // Clamp the zoom: don't zoom out further than 0.3x, and don't zoom in crazy close

        final float targetScale = Math.max(0.3f, Math.min(rawScale, 2.0f));


        // 4. Calculate target translation to perfectly center the math
        float contentCenterX = minX + (maxX - minX) / 2f;
        float contentCenterY = minY + (maxY - minY) / 2f;

        float targetTranslateX = (getWidth() / 2f) - (contentCenterX * targetScale);
        float targetTranslateY = (getHeight() / 2f) - (contentCenterY * targetScale);

        // 5. Animate the camera from its current position to the new target!
        ValueAnimator cameraAnimator = ValueAnimator.ofFloat(0f, 1f);
        cameraAnimator.setDuration(800); // 800ms smooth drone-camera swoosh
        cameraAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());

        final float startTranslateX = translateX;
        final float startTranslateY = translateY;
        final float startScale = scaleFactor;

        cameraAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            translateX = startTranslateX + (targetTranslateX - startTranslateX) * fraction;
            translateY = startTranslateY + (targetTranslateY - startTranslateY) * fraction;
            scaleFactor = startScale + (targetScale - startScale) * fraction;
            invalidate();
        });

        cameraAnimator.start();
    }
}