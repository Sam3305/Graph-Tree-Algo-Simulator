package com.example.graphtreealgosimulator.algorithms;

import com.example.graphtreealgosimulator.model.Graph;
import com.example.graphtreealgosimulator.model.Node;

public interface GraphAlgorithm {
    // Every algorithm must have a run method that takes these 3 exact parameters!
    void run(Graph graph, Node startNode, Node goalNode, AlgorithmListener listener);
}