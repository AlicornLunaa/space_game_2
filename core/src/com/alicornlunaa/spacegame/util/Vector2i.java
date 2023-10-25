package com.alicornlunaa.spacegame.util;

public class Vector2i implements Comparable<Vector2i> {
    // Variables
    public int x = 0;
    public int y = 0;

    // Constructors
    public Vector2i(){}
    
    public Vector2i(int x, int y){
        this.x = x;
        this.y = y;
    }

    // Functions
    public void set(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Vector2i other) {
        return (this.x * this.x + this.y * this.y) - (other.x * other.x + other.y * other.y);
    }
}
