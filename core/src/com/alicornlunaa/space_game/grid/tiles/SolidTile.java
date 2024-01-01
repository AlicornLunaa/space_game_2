package com.alicornlunaa.space_game.grid.tiles;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SolidTile extends TileElement {
    // Variables
    public boolean movable = false;

    // Constructor
    public SolidTile(Element element, int x, int y, TextureRegion texture, Collider collider) {
        super(element, State.SOLID, x, y, texture, collider);
    }
}
