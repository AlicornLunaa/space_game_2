package com.alicornlunaa.spacegame.components.tiles;

import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.badlogic.gdx.graphics.g2d.Batch;

public class StaticTileComponent extends TileComponent {
    // Variables
    public int x;
    public int y;

    // Constructor
    public StaticTileComponent(TextureComponent textureComponent, String tileID, int x, int y) {
        super(textureComponent, tileID);
        this.x = x;
        this.y = y;
    }

    // Functions
    @Override
    public void draw(Batch batch) {
    }
}
