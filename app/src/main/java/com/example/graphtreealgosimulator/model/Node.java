package com.example.graphtreealgosimulator.model;

import  java.util.Objects;

public class Node {

    private final int id;
    private float x;
    private float y;
    public Node(int id, float x, float y){
        this.id=id;
        this.x=x;
        this.y=y;
    }

    //setting values for x,y when moving dynamically
    public void setX(float x){
        this.x=x;
    }

    public void setY(float y){
        this.y=y;
    }

    //returns id ,x,y for the node
    public int getId(){
        return id;
    }
    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }


    @Override
    public boolean equals(Object o){
        if(this == o)   return true;
        //if object 'o' is not of the type 'node' obviously not equal
        if(!(o instanceof Node))    return false;
        //now type casting o into Node then comparing
        Node node= (Node) o;
        //my comparator
        return id == node.id;
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }
}