package com.alicornlunaa.space_game.util;

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
    public Vector2i set(int x, int y){
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2i cpy(){
        return new Vector2i(x, y);
    }

    @Override
    public boolean equals(Object o){
        if(o == this)
            return true;

        if(!(o instanceof Vector2i))
            return false;

        Vector2i v = (Vector2i)o;
        return v.x == x && v.y == y;
    }

    @Override
    public int compareTo(Vector2i other) {
        return (this.x * this.x + this.y * this.y) - (other.x * other.x + other.y * other.y);
    }
}
