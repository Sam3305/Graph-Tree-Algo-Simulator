package com.example.graphtreealgosimulator.algorithms;

import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.Node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BFSAlgorithm implements GraphAlgorithm{
    @Override
    public void run(Graph graph, Node startNode,Node goalNode,AlgorithmListener listener) {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();

        // Initial setup
        queue.add(startNode);
        visited.add(startNode);
        listener.onUpdateTerminal("Enqueue Node " + startNode.getId(), String.valueOf(startNode.getId()), true, "QUEUE");

        // Standard BFS Loop
        while (!queue.isEmpty()) {
            Node curr = queue.poll();

            // Tell the UI to update!
            listener.onUpdateTerminal("Dequeue Node " + curr.getId() + " (Processing)", String.valueOf(curr.getId()), false, "QUEUE");
            listener.onNodeProcessing(curr);
            listener.onNodeVisited(curr);

            for (Node neighbor : graph.getNeighbours(curr)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);

                    // Tell the UI we pushed a new neighbor!
                    listener.onUpdateTerminal("Enqueue Node " + neighbor.getId(), String.valueOf(neighbor.getId()), true, "QUEUE");
                }
            }
        }
    }
}