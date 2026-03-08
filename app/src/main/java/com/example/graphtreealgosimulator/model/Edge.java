package com.example.graphtreealgosimulator.model;

public class Edge {

    private final Node from;
    private final Node to;
    private int weight;
    public Edge(Node from, Node to,int weight){
        this.from=from;
        this.to=to;
        this.weight=weight;

    }
    public Node getFrom(){
        return from;
    }
    public Node getTo(){
        return to;

    }
    public int getWeight(){
        return weight;
    }
    public void setWeight(int weight){
        this.weight=weight;
    }
}
