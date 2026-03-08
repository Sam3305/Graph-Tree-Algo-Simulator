package com.example.graphtreealgosimulator.algorithms;

import com.example.graphtreealgosimulator.model.Edge;
import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.GraphType;
import com.example.graphtreealgosimulator.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BidirectionalAlgorithm implements GraphAlgorithm {

    @Override
    public void run(Graph graph, Node startNode, Node goalNode, AlgorithmListener listener) {
        if (goalNode == null) {
            listener.onFinalResultComputed("Bidirectional Error: Please select a Goal Node (Red) first!");
            return;
        }
        if (startNode.equals(goalNode)) {
            listener.onFinalResultComputed("Start and Goal are the same node!");
            return;
        }

        // We need TWO of everything!
        Queue<Node> fQueue = new LinkedList<>();
        Queue<Node> bQueue = new LinkedList<>();
        Set<Node> fVisited = new HashSet<>();
        Set<Node> bVisited = new HashSet<>();
        Map<Node, Node> fParent = new HashMap<>();
        Map<Node, Node> bParent = new HashMap<>();

        // Initialize Forward Search
        fQueue.add(startNode);
        fVisited.add(startNode);
        listener.onUpdateTerminal("[F] Enqueue " + startNode.getId(), String.valueOf(startNode.getId()), true, "QUEUE_F");

        // Initialize Backward Search
        bQueue.add(goalNode);
        bVisited.add(goalNode);
        listener.onUpdateTerminal("[B] Enqueue " + goalNode.getId(), String.valueOf(goalNode.getId()), true, "QUEUE_B");

        while (!fQueue.isEmpty() && !bQueue.isEmpty()) {
            // --- 1. EXPAND FORWARD SEARCH BY 1 NODE ---
            Node intersectNode = expandFrontier(fQueue, fVisited, bVisited, fParent, graph, listener, "[F]");
            if (intersectNode != null) {
                buildFinalPath(intersectNode, fParent, bParent, startNode, goalNode, listener);
                return;
            }

            // --- 2. EXPAND BACKWARD SEARCH BY 1 NODE ---
            intersectNode = expandFrontier(bQueue, bVisited, fVisited, bParent, graph, listener, "[B]");
            if (intersectNode != null) {
                buildFinalPath(intersectNode, fParent, bParent, startNode, goalNode, listener);
                return;
            }
        }

        listener.onFinalResultComputed("Frontiers never met! Goal is unreachable.");
    }

    // Helper method to expand a frontier by exactly one node
    private Node expandFrontier(Queue<Node> queue, Set<Node> myVisited, Set<Node> theirVisited, Map<Node, Node> parentMap, Graph graph, AlgorithmListener listener, String label) {
        if (queue.isEmpty()) return null;

        Node u = queue.poll();
        String targetQueue = label.equals("[F]") ? "QUEUE_F" : "QUEUE_B"; // Determine target
        listener.onUpdateTerminal(label + " Dequeue " + u.getId(), String.valueOf(u.getId()), false, targetQueue); // <-- Changed
        listener.onNodeProcessing(u);
        listener.onNodeVisited(u);

        for (Edge edge : graph.getEdges()) {
            Node neighbor = null;
            // Handle edge direction carefully based on which way we are searching!
            if (graph.getType() == GraphType.DIRECTED) {
                if (label.equals("[F]") && edge.getFrom().equals(u)) neighbor = edge.getTo();
                if (label.equals("[B]") && edge.getTo().equals(u)) neighbor = edge.getFrom(); // Backward search travels UP directed edges!
            } else {
                if (edge.getFrom().equals(u)) neighbor = edge.getTo();
                else if (edge.getTo().equals(u)) neighbor = edge.getFrom();
            }

            if (neighbor != null) {
                // DID WE JUST COLLIDE WITH THE OTHER SEARCH FRONTIER?!
                if (theirVisited.contains(neighbor)) {
                    parentMap.put(neighbor, u);
                    return neighbor; // Return the exact node where they crashed!
                }

                if (!myVisited.contains(neighbor)) {
                    myVisited.add(neighbor);
                    parentMap.put(neighbor, u);
                    queue.add(neighbor);
                    listener.onUpdateTerminal(label + " Enqueue " + neighbor.getId(), String.valueOf(neighbor.getId()), true, targetQueue);
                }
            }
        }
        return null; // No collision yet
    }

    // Stitches the two halves of the path together
    private void buildFinalPath(Node intersectNode, Map<Node, Node> fParent, Map<Node, Node> bParent, Node startNode, Node goalNode, AlgorithmListener listener) {
        List<Integer> path = new ArrayList<>();

        // 1. Trace from Intersect back to Start
        Node trace = intersectNode;
        while (trace != null) {
            path.add(trace.getId());
            trace = fParent.get(trace);
        }
        Collections.reverse(path); // Reverse so it goes Start -> Intersect

        // 2. Trace from Intersect forward to Goal
        trace = bParent.get(intersectNode);
        while (trace != null) {
            path.add(trace.getId());
            trace = bParent.get(trace);
        }

        StringBuilder finalOutput = new StringBuilder("Bidirectional Path Found:\n");
        for (int i = 0; i < path.size(); i++) {
            finalOutput.append(path.get(i));
            if (i < path.size() - 1) finalOutput.append(" ➔ ");
        }

        listener.onFinalResultComputed(finalOutput.toString());
    }
}