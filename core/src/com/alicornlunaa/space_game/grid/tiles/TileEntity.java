package com.alicornlunaa.space_game.grid.tiles;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class TileEntity extends AbstractTile {
    // Constructor
    public TileEntity(String tileID, int rotation, int width, int height) {
        super("te_" + tileID, rotation, width, height);
    }

    // Functions
    @Override
    public void render(Batch batch, float deltaTime){}

    @Override
    public void update(float deltaTime){}

    public boolean click(Buttons button){
        return false;
    }
}
