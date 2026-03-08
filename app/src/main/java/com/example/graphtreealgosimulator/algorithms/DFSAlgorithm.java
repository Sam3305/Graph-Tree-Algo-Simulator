package com.example.graphtreealgosimulator.algorithms;

import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class DFSAlgorithm implements GraphAlgorithm{
    @Override
    public void run(Graph graph, Node startNode, Node goalNode,AlgorithmListener listener) {
        Stack<Node> stack = new Stack<>();
        Set<Node> visited = new HashSet<>();

        stack.push(startNode);
        listener.onUpdateTerminal("Push Node " + startNode.getId() + " to Stack", String.valueOf(startNode.getId()), true, "STACK");

        // Standard DFS Loop
        while (!stack.isEmpty()) {
            Node curr = stack.pop();

            if (!visited.contains(curr)) {
                visited.add(curr);

                // Tell the UI to pop and color the node!
                listener.onUpdateTerminal("Pop Node " + curr.getId() + " (Processing)", String.valueOf(curr.getId()), false, "STACK");
                listener.onNodeProcessing(curr);
                listener.onNodeVisited(curr);

                // Get neighbors. (DFS pushes neighbors to stack)
                for (Node neighbor : graph.getNeighbours(curr)) {
                    if (!visited.contains(neighbor)) {
                        stack.push(neighbor);
                        listener.onUpdateTerminal("Push Node " + neighbor.getId() + " to Stack", String.valueOf(neighbor.getId()), true, "STACK");
                    }
                }
            } else {
                // If it was already visited, just visually pop it and skip
                listener.onUpdateTerminal("Pop Node " + curr.getId() + " (Already Visited)", String.valueOf(curr.getId()), false, "STACK");
            }
        }
    }
}