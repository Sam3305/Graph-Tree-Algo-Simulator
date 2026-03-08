package com.example.graphtreealgosimulator.model;

import java.util.List;
import java.util.ArrayList;
public class Graph {

    private GraphType type;
    private final List<Node> nodes;

    private final List<Edge> edges;


    public Graph(GraphType type){
        this.type=type;
        this.nodes= new ArrayList<>();
        this.edges= new ArrayList<>();
    }

    public void setType(GraphType type){
        this.type=type;
    }

    public GraphType getType(){
        return type;
    }

    public List<Node> getNodes(){
        return nodes;
    }

    public List<Edge> getEdges(){
        return edges;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }
    public void removeNode(Node node) {
        nodes.remove(node);

        edges.removeIf(edge ->
                edge.getFrom().equals(node) ||
                        edge.getTo().equals(node)
        );
    }

    public boolean addEdge(Node from, Node to, int weight){
        // Prevent self-loops (node connecting to itself)
        if(from.equals(to)){
            return false;
        }

        // Prevent duplicate edges
        for (Edge e : edges) {
            if (type == GraphType.DIRECTED) {
                // For directed: exactly A -> B
                if (e.getFrom().equals(from) && e.getTo().equals(to)) {
                    return false;
                }
            } else {
                // For undirected: A -> B or B -> A are the same edge
                if ((e.getFrom().equals(from) && e.getTo().equals(to)) ||
                        (e.getFrom().equals(to) && e.getTo().equals(from))) {
                    return false;
                }
            }
        }

        // Everything passed! Create the edge correctly.
        edges.add(new Edge(from, to, weight));
        return true;
    }


    //TRAVERSAL
    public List<Node> getNeighbours(Node node){

        List<Node> neighbours=new ArrayList<>();
        for (Edge e: edges){
            if(type==GraphType.DIRECTED){
                if(e.getFrom().equals(node)){
                    neighbours.add(e.getTo());
                }
            }
            else{
                //UNDIRECTED
                if(e.getFrom().equals(node) ){
                    neighbours.add(e.getTo());
                } else if (e.getTo().equals(node)) {
                    neighbours.add(e.getFrom());
                }
            }
        }
        return neighbours;
    }
    // Clears all nodes and edges from the graph
    public void clear() {
        nodes.clear();
        edges.clear();
    }
}
