package com.alicornlunaa.space_game.grid.tiles;

import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class AbstractTile {
    // Enumerations
    public static enum Direction { NORTH, EAST, SOUTH, WEST };
    
    // Variables
    public final String tileID;
    public final int width, height;
    public int x, y, rotation;
    public boolean updatedThisTick = false;

    // Constructor
    public AbstractTile(String tileID, int x, int y, int rotation, int width, int height){
        this.tileID = tileID;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    public AbstractTile(AbstractTile other){
        this.tileID = other.tileID;
        this.width = other.width;
        this.height = other.height;
        this.x = other.x;
        this.y = other.y;
        this.rotation = other.rotation;
    }

    // Functions
    public abstract void render(Batch batch, float deltaTime);
    public abstract void update(float deltaTime);
}
