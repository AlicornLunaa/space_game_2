package com.alicornlunaa.space_game.grid.tiles;

public abstract class AbstractTile {
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
}
