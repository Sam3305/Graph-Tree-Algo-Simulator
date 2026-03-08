package com.example.graphtreealgosimulator.algorithms;

public class AlgorithmFactory {

    public static GraphAlgorithm getAlgorithm(String algorithmName) {
        if (algorithmName == null) return null;

        switch (algorithmName.toUpperCase()) {
            case "BFS":
                return new BFSAlgorithm();
            case "DFS":
                return new DFSAlgorithm();
            case "DIJKSTRA":
                return new DijkstraAlgorithm();
            case "A*":
                return new AStarAlgorithm();
            case "UCS":
                return new UCSAlgorithm();
            case "DLS":
                return new DLSAlgorithm();
            case "BIDIRECTIONAL":
                return new BidirectionalAlgorithm();
            // In the future, just add a new case returning the algorithm
            default:
                throw new IllegalArgumentException("Unknown Algorithm: " + algorithmName);
        }
    }
}