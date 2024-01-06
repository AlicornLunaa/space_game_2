package com.alicornlunaa.space_game.grid.entities;

import org.json.JSONArray;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ThrusterTile extends TileEntity {
    private TextureRegion texture;

    public ThrusterTile(int rotation) {
        super("thruster", 1, 1, rotation);

        collider = new Collider(new JSONArray(Gdx.files.internal("assets/colliders/parts/bsc_thruster.json").readString()));
        texture = App.instance.atlas.findRegion("parts/bsc_thruster");
    }
    
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
    public void update(float deltaTime) {
    }
}
