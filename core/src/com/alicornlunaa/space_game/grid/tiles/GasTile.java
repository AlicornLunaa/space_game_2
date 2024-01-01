package com.alicornlunaa.space_game.grid.tiles;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GasTile extends TileElement {
    public GasTile(Element element, int x, int y, TextureRegion texture, Collider collider) {
        super(element, State.GAS, x, y, texture, collider);
    }
}
