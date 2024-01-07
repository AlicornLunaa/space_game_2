package com.alicornlunaa.space_game.grid.entities;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CustomTile extends TileEntity {
    private TextureRegion texture;

    public CustomTile(int rotation) {
        super("custom", 4, 2, rotation);

        collider = Collider.box(0, 0, 4 * Constants.TILE_SIZE / 2.f, 2 * Constants.TILE_SIZE / 2.f, 0);

        Pixmap textureData = new Pixmap(1, 1, Format.RGBA8888);
        textureData.setColor(0, 1, 0, 1);
        textureData.fill();
        texture = new TextureRegion(new Texture(textureData));
        textureData.dispose();
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
    public void update(Entity entity, float deltaTime) {
    }
}
