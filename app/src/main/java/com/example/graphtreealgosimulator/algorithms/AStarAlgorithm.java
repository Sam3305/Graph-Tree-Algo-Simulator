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

public class AStarAlgorithm implements GraphAlgorithm {

    private static class AStarNode {
        Node node;
        int fCost; // Total Cost (gCost + hCost)
        int gCost; // Exact cost from Start to this node

        AStarNode(Node node, int fCost, int gCost) {
            this.node = node;
            this.fCost = fCost;
            this.gCost = gCost;
        }
    }

    // HEURISTIC: Calculate the straight-line physical distance between two nodes
    private int calculateHeuristic(Node a, Node b) {
        if (a == null || b == null) return 0;
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        // We divide by 100 to scale the pixels down to standard edge weight proportions
        return (int) (Math.sqrt(dx * dx + dy * dy) / 100f);
    }

    @Override
    public void run(Graph graph, Node startNode, Node goalNode, AlgorithmListener listener) {
        if (goalNode == null) {
            listener.onFinalResultComputed("A* Error: Please select a Goal Node (Red) first!");
            return;
        }

        Map<Node, Integer> gCosts = new HashMap<>();
        Map<Node, Node> parentMap = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        PriorityQueue<AStarNode> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));

        for (Node n : graph.getNodes()) gCosts.put(n, Integer.MAX_VALUE);

        gCosts.put(startNode, 0);
        int startH = calculateHeuristic(startNode, goalNode);
        pq.add(new AStarNode(startNode, startH, 0));

        listener.onUpdateTerminal(
                "Enqueue " + startNode.getId() + " (f: " + startH + ")",
                startNode.getId() + "|" + startH + "|0 + " + startH, // <-- The new payload!
                true,
                "PRIORITY_QUEUE"
        );
        while (!pq.isEmpty()) {
            AStarNode curr = pq.poll();
            Node u = curr.node;

            if (visited.contains(u)) continue;

            listener.onUpdateTerminal(
                    "Dequeue " + u.getId() + " (Processing)",
                    u.getId() + "|" + curr.fCost + "|" + curr.gCost + " + " + (curr.fCost - curr.gCost), // <-- The new payload!
                    false,
                    "PRIORITY_QUEUE"
            );
            listener.onNodeProcessing(u);
            visited.add(u);
            listener.onNodeVisited(u);

            // A* OPTIMIZATION: If we reached the goal, STOP SEARCHING immediately!
            if (u.equals(goalNode)) {
                buildFinalPath(startNode, goalNode, parentMap, gCosts.get(goalNode), listener);
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
                    int tentativeGCost = gCosts.get(u) + edge.getWeight();

                    if (tentativeGCost < gCosts.get(neighbor)) {
                        gCosts.put(neighbor, tentativeGCost);
                        parentMap.put(neighbor, u);

                        int hCost = calculateHeuristic(neighbor, goalNode);
                        int fCost = tentativeGCost + hCost; // F = G + H

                        pq.add(new AStarNode(neighbor, fCost, tentativeGCost));
                        listener.onUpdateTerminal(
                                "Update " + neighbor.getId() + " (f: " + fCost + ")",
                                neighbor.getId() + "|" + fCost + "|" + tentativeGCost + " + " + hCost, // <-- The new payload!
                                true,
                                "PRIORITY_QUEUE"
                        );
                    }
                }
            }
        }
        listener.onFinalResultComputed("Goal Node is unreachable!");
    }

    private void buildFinalPath(Node startNode, Node goalNode, Map<Node, Node> parentMap, int totalCost, AlgorithmListener listener) {
        java.util.List<Integer> path = new java.util.ArrayList<>();
        Node traceNode = goalNode;
        while (traceNode != null) {
            path.add(0, traceNode.getId());
            traceNode = parentMap.get(traceNode);
        }

        StringBuilder finalOutput = new StringBuilder("A* Optimal Path:\n");
        for (int i = 0; i < path.size(); i++) {
            finalOutput.append(path.get(i));
            if (i < path.size() - 1) finalOutput.append(" ➔ ");
        }
        finalOutput.append("\n(Total Cost: ").append(totalCost).append(")");
        listener.onFinalResultComputed(finalOutput.toString());
    }
}