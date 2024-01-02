package com.alicornlunaa.space_game.grid.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SolidTile extends TileElement {
    // Variables
    public boolean movable = false;

    // Constructor
    public SolidTile(Element element, TextureRegion texture) {
        super(element, State.SOLID, texture);
    }
}
