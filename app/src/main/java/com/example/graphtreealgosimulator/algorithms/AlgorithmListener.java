package com.example.graphtreealgosimulator.algorithms;

import com.example.graphtreealgosimulator.model.Node;

public interface AlgorithmListener {
    // Shouts out when the terminal needs to push="true"/pop="false"
    void onUpdateTerminal(String actionText, String nodeVal, boolean isPush, String structureType);

    // Shouts out to color the node yellow
    void onNodeProcessing(Node node);

    // Shouts out to color the node green
    void onNodeVisited(Node node);
    default void onFinalResultComputed(String result) {}
}