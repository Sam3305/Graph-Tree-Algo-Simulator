package com.example.graphtreealgosimulator.algorithms;

import com.example.graphtreealgosimulator.model.Edge;
import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.GraphType;
import com.example.graphtreealgosimulator.model.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class DLSAlgorithm implements GraphAlgorithm {

    private int depthLimit = 3; // Hardcoded for testing! We can add a UI input for this later.

    // Helper class to tie a node to its current depth in the tree
    private static class DLSNode {
        Node node;
        int depth;

        DLSNode(Node node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }
    public void setDepthLimit(int depthLimit) {
        this.depthLimit = depthLimit;
    }
    @Override
    public void run(Graph graph, Node startNode, Node goalNode, AlgorithmListener listener) {
        Stack<DLSNode> stack = new Stack<>();
        Set<Node> visited = new HashSet<>();

        // Start at depth 0
        stack.push(new DLSNode(startNode, 0));

        // we use "STACK" here so TerminalManager knows to stack them vertically!
        listener.onUpdateTerminal("Push " + startNode.getId() + " (Depth: 0)", String.valueOf(startNode.getId()), true, "STACK");

        while (!stack.isEmpty()) {
            DLSNode curr = stack.pop();
            Node u = curr.node;

            listener.onUpdateTerminal("Pop " + u.getId() + " (Processing)", String.valueOf(u.getId()), false, "STACK");

            // Skip if already visited
            if (visited.contains(u)) continue;

            listener.onNodeProcessing(u);
            visited.add(u);
            listener.onNodeVisited(u);

            // 1. EARLY EXIT: Did we find the goal?
            if (goalNode != null && u.equals(goalNode)) {
                listener.onFinalResultComputed("Target found at depth " + curr.depth + "!");
                return;
            }

            // 2. THE DLS RULE: Only add children if we haven't hit the limit!
            if (curr.depth < depthLimit) {
                for (Edge edge : graph.getEdges()) {
                    Node neighbor = null;
                    if (graph.getType() == GraphType.DIRECTED) {
                        if (edge.getFrom().equals(u)) neighbor = edge.getTo();
                    } else {
                        if (edge.getFrom().equals(u)) neighbor = edge.getTo();
                        else if (edge.getTo().equals(u)) neighbor = edge.getFrom();
                    }

                    if (neighbor != null && !visited.contains(neighbor)) {
                        stack.push(new DLSNode(neighbor, curr.depth + 1));
                        listener.onUpdateTerminal("Push " + neighbor.getId() + " (Depth: " + (curr.depth + 1) + ")", String.valueOf(neighbor.getId()), true, "STACK");
                    }
                }
            } else {
                // We hit the limit! We literally just do nothing and let the loop continue
                // so it is forced to back-track up the tree.
            }
        }

        // If the stack empties and we never found the goal...
        listener.onFinalResultComputed("DLS Complete. Max Depth Reached: " + depthLimit);
    }
}