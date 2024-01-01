package com.alicornlunaa.space_game.grid.tiles;

public abstract class TileEntity extends AbstractTile {
    // Constructor
    public TileEntity(String tileID, int x, int y, int rotation, int width, int height) {
        super("te_" + tileID, x, y, rotation, width, height);
    }

    // Functions
    public abstract void update(float deltaTime);
}
