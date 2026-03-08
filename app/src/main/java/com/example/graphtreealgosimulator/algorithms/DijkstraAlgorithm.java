package com.example.graphtreealgosimulator.algorithms;

import com.example.graphtreealgosimulator.model.Edge;
import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.GraphType;
import com.example.graphtreealgosimulator.model.Node;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraAlgorithm implements GraphAlgorithm {

    // Helper class to store a node and its current shortest distance
    private static class NodeDistance {
        Node node;
        int distance;

        NodeDistance(Node node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    @Override
    public void run(Graph graph, Node startNode, Node goalNode,AlgorithmListener listener) {
        Map<Node, Integer> distances = new HashMap<>();
        Map<Node, Node> parentMap = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.distance));

        for (Node n : graph.getNodes()) {
            distances.put(n, Integer.MAX_VALUE);
        }
        distances.put(startNode, 0);
        pq.add(new NodeDistance(startNode, 0));

        listener.onUpdateTerminal("Enqueue " + startNode.getId() + " (Dist: 0)", startNode.getId() + "|0", true, "PRIORITY_QUEUE");

        while (!pq.isEmpty()) {
            NodeDistance curr = pq.poll();
            Node u = curr.node;

            if (visited.contains(u)) continue;

            listener.onUpdateTerminal("Dequeue " + u.getId() + " (Processing)", u.getId() + "|" + distances.get(u), false, "PRIORITY_QUEUE");            listener.onNodeProcessing(u);

            visited.add(u);
            listener.onNodeVisited(u);

            for (Edge edge : graph.getEdges()) {
                Node neighbor = null;
                if (graph.getType() == GraphType.DIRECTED) {
                    if (edge.getFrom().equals(u)) neighbor = edge.getTo();
                } else {
                    if (edge.getFrom().equals(u)) neighbor = edge.getTo();
                    else if (edge.getTo().equals(u)) neighbor = edge.getFrom();
                }

                if (neighbor != null && !visited.contains(neighbor)) {
                    int newDist = distances.get(u) + edge.getWeight();

                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);

                        parentMap.put(neighbor, u);

                        pq.add(new NodeDistance(neighbor, newDist));
                        listener.onUpdateTerminal("Update " + neighbor.getId() + " (Dist: " + newDist + ")", neighbor.getId() + "|" + newDist, true, "PRIORITY_QUEUE");                    }
                }
            }
        }

        StringBuilder finalOutput = new StringBuilder("Shortest Paths:\n");

        for (Node target : graph.getNodes()) {
            if (target.equals(startNode)) continue; // Skip the start node

            if (distances.get(target) == Integer.MAX_VALUE) {
                finalOutput.append(startNode.getId()).append(" -> ").append(target.getId()).append(" : Unreachable\n");
            } else {
                // Trace the breadcrumbs backward from target to start
                java.util.List<Integer> path = new java.util.ArrayList<>();
                Node traceNode = target;
                while (traceNode != null) {
                    path.add(0, traceNode.getId()); // Always insert at the front to reverse it!
                    traceNode = parentMap.get(traceNode);
                }

                // Format the string: "1 ➔ 2 ➔ 4 (Cost: 15)"
                for (int i = 0; i < path.size(); i++) {
                    finalOutput.append(path.get(i));
                    if (i < path.size() - 1) finalOutput.append(" ➔ ");
                }
                finalOutput.append(" (Cost: ").append(distances.get(target)).append(")\n");
            }
        }

        // Shout out the final constructed string to the UI!
        listener.onFinalResultComputed(finalOutput.toString());
    }
}