package com.alicornlunaa.spacegame.components.tiles;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;

public class DynamicTileComponent extends TileComponent {
    // Variables
    private BodyComponent bodyComponent;

    // Constructor
    public DynamicTileComponent(TextureComponent textureComponent, BodyComponent bodyComponent, String tileID) {
        super(textureComponent, tileID);
        this.bodyComponent = bodyComponent;
    }

    // Functions
    @Override
    public void draw(Batch batch) {
        batch.draw(
            textureComponent.texture,
            bodyComponent.body.getWorldCenter().x,
            bodyComponent.body.getWorldCenter().y,
            0,
            0,
            Constants.TILE_SIZE,
            Constants.TILE_SIZE,
            1, 1,
            bodyComponent.body.getAngle()
        );
    }
}
