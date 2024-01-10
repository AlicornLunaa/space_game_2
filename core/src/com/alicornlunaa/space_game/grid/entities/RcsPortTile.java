package com.alicornlunaa.space_game.grid.entities;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RcsPortTile extends TileEntity {
    // Variables
    private TextureRegion texture;

    // Constructor
    public RcsPortTile(int rotation) {
        super("rcs_port", 1, 1, rotation);
        texture = App.instance.atlas.findRegion("parts/rcs_port");
    }
    
    // Functions
    @Override
    public void render(Batch batch, float deltaTime){
        batch.draw(
            texture,
            x * Constants.TILE_SIZE,
            y * Constants.TILE_SIZE,
            Constants.TILE_SIZE / 2.f,
            Constants.TILE_SIZE / 2.f,
            Constants.TILE_SIZE * width,
            Constants.TILE_SIZE * height,
            1,
            1,
            rotation * -90
        );
    }

    @Override
    public void update(Entity entity, float deltaTime) {
    }
}