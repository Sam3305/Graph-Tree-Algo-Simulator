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

public class UCSAlgorithm implements GraphAlgorithm {

    // Helper class to store a node and its path cost
    private static class UCSNode {
        Node node;
        int cost;

        UCSNode(Node node, int cost) {
            this.node = node;
            this.cost = cost;
        }
    }

    @Override
    public void run(Graph graph, Node startNode, Node goalNode, AlgorithmListener listener) {
        if (goalNode == null) {
            listener.onFinalResultComputed("UCS Error: Please select a Goal Node (Red) first!");
            return;
        }

        Map<Node, Integer> costs = new HashMap<>();
        Map<Node, Node> parentMap = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        // Priority Queue sorts purely by the accumulated cost
        PriorityQueue<UCSNode> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        for (Node n : graph.getNodes()) {
            costs.put(n, Integer.MAX_VALUE);
        }
        costs.put(startNode, 0);

        pq.add(new UCSNode(startNode, 0));

        // We use our standard Priority Queue payload without the custom A* 3rd parameter!
        listener.onUpdateTerminal("Enqueue " + startNode.getId() + " (Cost: 0)", startNode.getId() + "|0", true, "PRIORITY_QUEUE");

        while (!pq.isEmpty()) {
            UCSNode curr = pq.poll();
            Node u = curr.node;

            // Ignore stale paths
            if (visited.contains(u)) continue;

            listener.onUpdateTerminal("Dequeue " + u.getId() + " (Processing)", u.getId() + "|" + curr.cost, false, "PRIORITY_QUEUE");
            listener.onNodeProcessing(u);
            visited.add(u);
            listener.onNodeVisited(u);

            // ============================================
            // UCS EARLY EXIT: Stop when goal is reached!
            // ============================================
            if (u.equals(goalNode)) {
                buildFinalPath(startNode, goalNode, parentMap, curr.cost, listener);
                return;
            }

            for (Edge edge : graph.getEdges()) {
                Node neighbor = null;
                if (graph.getType() == GraphType.DIRECTED) {
                    if (edge.getFrom().equals(u)) neighbor = edge.getTo();
                } else {
                    if (edge.getFrom().equals(u)) neighbor = edge.getTo();
                    else if (edge.getTo().equals(u)) neighbor = edge.getFrom();
                }

                if (neighbor != null && !visited.contains(neighbor)) {
                    int newCost = costs.get(u) + edge.getWeight();

                    if (newCost < costs.get(neighbor)) {
                        costs.put(neighbor, newCost);
                        parentMap.put(neighbor, u);

                        pq.add(new UCSNode(neighbor, newCost));
                        listener.onUpdateTerminal("Update " + neighbor.getId() + " (Cost: " + newCost + ")", neighbor.getId() + "|" + newCost, true, "PRIORITY_QUEUE");
                    }
                }
            }
        }
        // If the queue empties and we never hit the return statement above...
        listener.onFinalResultComputed("Goal Node is unreachable!");
    }

    private void buildFinalPath(Node startNode, Node goalNode, Map<Node, Node> parentMap, int totalCost, AlgorithmListener listener) {
        java.util.List<Integer> path = new java.util.ArrayList<>();
        Node traceNode = goalNode;
        while (traceNode != null) {
            path.add(0, traceNode.getId());
            traceNode = parentMap.get(traceNode);
        }

        StringBuilder finalOutput = new StringBuilder("UCS Optimal Path:\n");
        for (int i = 0; i < path.size(); i++) {
            finalOutput.append(path.get(i));
            if (i < path.size() - 1) finalOutput.append(" ➔ ");
        }
        finalOutput.append("\n(Total Cost: ").append(totalCost).append(")");

        // Shout out the final result to the Canvas!
        listener.onFinalResultComputed(finalOutput.toString());
    }
}