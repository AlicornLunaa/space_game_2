package com.alicornlunaa.space_game.grid.tiles;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Null;

public abstract class TileEntity extends AbstractTile {
    // Variables
    public @Null Collider collider = null;

    // Constructor
    public TileEntity(String tileID, int width, int height, int rotation) {
        super("te_" + tileID, width, height, rotation);
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
